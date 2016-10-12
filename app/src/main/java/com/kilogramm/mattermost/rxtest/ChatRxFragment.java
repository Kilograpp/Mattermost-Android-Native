package com.kilogramm.mattermost.rxtest;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.AdapterPost;
import com.kilogramm.mattermost.adapters.UsersDropDownListAdapter;
import com.kilogramm.mattermost.databinding.EditDialogLayoutBinding;
import com.kilogramm.mattermost.databinding.FragmentChatMvpBinding;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.ui.MRealmRecyclerView;
import com.kilogramm.mattermost.view.chat.NewChatListAdapter;
import com.kilogramm.mattermost.view.chat.OnItemAddedListener;
import com.kilogramm.mattermost.view.chat.OnItemClickListener;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

import java.util.Calendar;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 06.10.2016.
 */
@RequiresPresenter(ChatRxPresenter.class)
public class ChatRxFragment extends BaseFragment<ChatRxPresenter> implements OnItemAddedListener,
        OnItemClickListener<Post>,OnMoreLoadListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "ChatRxFragment";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";

    private static final Integer TYPING_DURATION = 5000;

    private FragmentChatMvpBinding binding;

    @State
    String channelId;

    @State
    String teamId;

    @State
    String channelName;

    private Realm realm;

    private AdapterPost adapter;
    private UsersDropDownListAdapter dropDownListAdapter;

    private PostRepository postRepository;
    private UserRepository userRepository;

    private BroadcastReceiver brReceiverTyping;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelId = getArguments().getString(CHANNEL_ID);
        this.channelName = getArguments().getString(CHANNEL_NAME);
        this.realm = Realm.getDefaultInstance();
        this.teamId = realm.where(Team.class).findFirst().getId();
        this.postRepository = new PostRepository();
        this.userRepository = new UserRepository();
        getPresenter().initPresenter(teamId,channelId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_mvp,
                container, false);
        View view = binding.getRoot();
        initView();
        return view;
    }

    private void initView() {
        setupListChat(channelId);
        setupRefreshListener();
        setBtnSendOnClickListener();
        setDropDownUserList();
        brReceiverTyping = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WebSocketObj obj = intent.getParcelableExtra(MattermostService.BROADCAST_MESSAGE);
                Log.d(TAG,obj.getEvent());
                if(obj.getChannelId().equals(channelId)){
                    getActivity().runOnUiThread(() -> showTyping());
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(WebSocketObj.EVENT_TYPING);
        getActivity().registerReceiver(brReceiverTyping, intentFilter);
        getPresenter().requestExtraInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPresenter().initPresenter(teamId,channelId);
        setupToolbar("",channelName,v -> Toast.makeText(getActivity().getApplicationContext(), "In development", Toast.LENGTH_SHORT).show());
    }

    private void setDropDownUserList() {
        dropDownListAdapter = new UsersDropDownListAdapter(this::addUserLinkMessage);
        binding.idRecUser.setAdapter(dropDownListAdapter);
        binding.idRecUser.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.writingMessage.addTextChangedListener(getPresenter().getMassageTextWatcher());
    }

    public void setDropDown(RealmResults<User> realmResult){
        dropDownListAdapter.setUsers(realmResult);
    }

    public static ChatRxFragment createFragment(String channelId, String channelName){
        ChatRxFragment chatFragment = new ChatRxFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CHANNEL_ID, channelId);
        bundle.putString(CHANNEL_NAME, channelName);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    private void setupListChat(String channelId) {
        RealmResults<Post> results = postRepository.query(new PostByChannelId(channelId));
        results.addChangeListener(element -> {
            if(adapter!=null){
                if(results.size()-2 == ((LinearLayoutManager) binding.rev.getLayoutManager()).findLastCompletelyVisibleItemPosition()){
                    onItemAdded();
                }
            }
        });
        adapter = new AdapterPost(getActivity(),results, this);
        //adapter = new NewChatListAdapter(getActivity(), results, true, this);
        binding.rev.setAdapter(adapter);
        binding.rev.setListener(this);
        //setupPaginationListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.realm.close();
        Log.d(TAG, "onDestroy()");
        channelId = null;
        getActivity().unregisterReceiver(brReceiverTyping);
    }



    public String getChId(){
        return this.channelId;
    }

    public void setBtnSendOnClickListener(){
        binding.btnSend.setOnClickListener(view -> sendMessage());
    }

    //==========================MVP methods==================================================


    private void sendMessage() {
        Post post = new Post();
        post.setChannelId(channelId);
        post.setCreateAt(Calendar.getInstance().getTimeInMillis());
        post.setMessage(getMessage());
        post.setUserId(MattermostPreference.getInstance().getMyUserId());
        //post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
        post.setId(String.format("%s:%s", post.getUserId(), post.getCreateAt()));
        post.setPendingPostId(String.format("%s:%s", post.getUserId(), post.getCreateAt()));
        setMessage("");
        if(post.getMessage().length() != 0){
            getPresenter().requestSendToServer(post);
            //WebSocketService.with(context).sendTyping(channelId, teamId.getId());
        } else {
            Toast.makeText(getActivity(), "Message is empty", Toast.LENGTH_SHORT).show();
        }
    } // +

    private void setupRefreshListener() {
        binding.rev.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int bottomRow =
                        (recyclerView == null || recyclerView.getChildCount() == 0)
                                ? 0
                                : recyclerView.getAdapter().getItemCount()-1;

                if(bottomRow == ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findLastCompletelyVisibleItemPosition()){
                    binding.swipeRefreshLayout
                            .setEnabled(true);
                } else {
                    binding.swipeRefreshLayout
                            .setEnabled(false);
                }


            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        binding.swipeRefreshLayout.setOnRefreshListener(direction -> {
            //getPresenter().initLoadNext();
            Log.d("DISABLE", "disable loading");
            binding.rev.disableShowLoadMoreTop();
            binding.rev.disableShowLoadMoreBot();
            binding.rev.setCanPagination(false);

            postRepository.remove(new PostByChannelId(channelId));
            getPresenter().requestLoadPosts();
        });
    }

//    private void setupPaginationListener(){
//        binding.rev.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                tryPagination();
//            }
//        });
//    }

//    private void tryPagination() {
//        int firstvisibleItem = ((LinearLayoutManager) binding.rev.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
//        int lastvisibleItem = ((LinearLayoutManager) binding.rev.getLayoutManager()).findLastCompletelyVisibleItemPosition();
//        int countItems = binding.rev.getAdapter().getItemCount();
//        if(!showShowLoadMoreTop) {
////            Log.d(TAG, "Log scrolling recyclerview: \n" +
////                    "findFirstCompletelyVisibleItemPosition = " + firstvisibleItem + "\n" +
////                    "findLastCompletelyVisibleItemPosition = " + lastvisibleItem + "\n" +
////                    "countItem = " + binding.rev.getAdapter().getItemCount());
//            if (firstvisibleItem < 2 && !((AdapterPost) binding.rev.getAdapter()).getTopLoading()) {
//                Log.d(TAG, "Top Loader");
//                this.enableShowLoadMoreTop();
//                //((AdapterPost) binding.rev.getAdapter()).setLoadingTop(true);
//            } else {
//                // ((AdapterPost) binding.rev.getAdapter()).setLoadingTop(false);
//            }
//        }
//        if(!showShowLoadMoreBot) {
//            if (countItems - lastvisibleItem < 2 && !((AdapterPost) binding.rev.getAdapter()).getBottomLoading()) {
//                Log.d(TAG, "Bottom loader");
//                this.enableShowLoadMoreBot();
//                //((AdapterPost) binding.rev.getAdapter()).setLoadingBottom(true);
//            } else {
//                //((AdapterPost) binding.rev.getAdapter()).setLoadingBottom(false);
//            }
//        }
//
//    }

    public void showEmptyList() {
        Log.d(TAG, "showEmptyList()");
        binding.progressBar.setVisibility(View.GONE);
        binding.emptyList.setVisibility(View.VISIBLE);
        binding.newMessageLayout.setVisibility(View.VISIBLE);
    }

    public void showList() {
        Log.d(TAG, "showList()");
        binding.progressBar.setVisibility(View.GONE);
        binding.rev.setVisibility(View.VISIBLE);
        binding.newMessageLayout.setVisibility(View.VISIBLE);
    }

    public void setRefreshing(boolean b) {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.rev.setCanPagination(true);
        binding.rev.setCanPaginationTop(true);
        binding.rev.setCanPaginationBot(true);
    }

    public void showTyping(){
        binding.typing.setVisibility(View.VISIBLE);
        binding.typing.postDelayed(() -> binding.typing.setVisibility(View.GONE), TYPING_DURATION);
    }

    public String getMessage() {
        return binding.writingMessage.getText().toString();
    }

    public void setMessage(String s){
        binding.writingMessage.setText(s);
    }



    @Override
    public void onItemAdded() {
        binding.rev.smoothScrollToPosition(binding.rev.getAdapter().getItemCount()-1);
    }

    public void addUserLinkMessage(String s){
        binding.writingMessage.append(s + " ");
    }

    @Override
    public void OnItemClick(View view, Post item) {
        switch (view.getId()){
            case R.id.controlMenu:
                showPopupMenu(view, item);
                break;
        }
    }

    private void showPopupMenu(View view, Post post) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view, Gravity.BOTTOM);
        if(post.getUserId().equals(MattermostPreference.getInstance().getMyUserId())){
            popupMenu.inflate(R.menu.my_chat_item_popupmenu);
        } else {
            popupMenu.inflate(R.menu.foreign_chat_item_popupmenu);
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            EditDialogLayoutBinding binding = DataBindingUtil.inflate(getActivity().getLayoutInflater(),
                    R.layout.edit_dialog_layout,null,false);
            switch (menuItem.getItemId()){
                case R.id.edit:
                    showEditView(Html.fromHtml(post.getMessage()).toString());
                    break;
                case R.id.delete:
                    new AlertDialog.Builder(getActivity(),R.style.AppCompatAlertDialogStyle)
                            .setTitle(getString(R.string.confirm_post_delete))
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                                getPresenter().requestDeletePost(post);
                            })
                            .show();
                    break;
                case R.id.permalink:
                    binding.edit.setText(getMessageLink(post.getId()));
                    new AlertDialog.Builder(getActivity(),R.style.AppCompatAlertDialogStyle)
                            .setTitle(getString(R.string.copy_permalink))
                            .setView(binding.getRoot())
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.copy_link, (dialogInterface1, i1) -> {
                                copyLink(binding.edit.getText().toString());
                            })
                            .show();
                    break;
                case R.id.reply:
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void showEditView(String message) {
        Animation fallingAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.edit_card_anim);
        Animation upAnim = AnimationUtils.loadAnimation(getActivity(),R.anim.edit_card_up);
        binding.editMessageLayout.editableText.setText(message);
        binding.editMessageLayout.root.startAnimation(upAnim);
        //binding.editMessageLayout.card.startAnimation(fallingAnimation);
        binding.editMessageLayout.getRoot().setVisibility(View.VISIBLE);
    }

    private String getMessageLink(String postId) {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/"
                + realm.where(Team.class).findFirst().getName()
                + "/pl/"
                + postId;
    }

    public void invalidateAdapter() {
        adapter.notifyDataSetChanged();
    }

    public void copyLink(String link){
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(link);
        Toast.makeText(getActivity(),"link copied",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTopLoadMore() {
        Log.d(TAG,"onTopLoadMore()");
        getPresenter().requestLoadBefore();
    }

    @Override
    public void onBotLoadMore() {
        Log.d(TAG,"onBotLoadMore()");
        getPresenter().requestLoadAfter();
    }

    public void disableShowLoadMoreTop() {
        Log.d(TAG,"disableShowLoadMoreTop()");
        binding.rev.disableShowLoadMoreTop();
    }

    public void disableShowLoadMoreBot() {
        Log.d(TAG,"disableShowLoadMoreBot()");
        binding.rev.disableShowLoadMoreBot();
    }

    public void setCanPaginationTop(Boolean aBoolean) {
        disableShowLoadMoreTop();
        binding.rev.setCanPaginationTop(aBoolean);
    }

    public void setCanPaginationBot(Boolean aBoolean) {
        disableShowLoadMoreBot();
        binding.rev.setCanPaginationBot(aBoolean);
    }
}
