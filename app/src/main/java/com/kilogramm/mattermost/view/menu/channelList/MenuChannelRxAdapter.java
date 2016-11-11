package com.kilogramm.mattermost.view.menu.channelList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.member.MemberById;
import com.kilogramm.mattermost.model.entity.member.MembersRepository;
import com.kilogramm.mattermost.ui.CheckableLinearLayout;

import io.realm.RealmAD;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 24.10.2016.
 */
public class MenuChannelRxAdapter extends RealmAD<Channel, MenuChannelListHolder> {

    private static final String TAG = "MenuChannelRxAdapter";

    private Context context;
    private LayoutInflater inflater;

    private MenuChannelListFragment.OnChannelItemClickListener channelItemClickListener;
    private MenuChannelListFragment.OnSelectedItemChangeListener selectedItemChangeListener;
    private int selecteditem = -1;

    public MenuChannelRxAdapter(Context context, RealmResults<Channel> adapterData,
                                MenuChannelListFragment.OnChannelItemClickListener listener) {
        super(adapterData);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.channelItemClickListener = listener;
    }

    @Override
    public MenuChannelListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MenuChannelListHolder holder = MenuChannelListHolder.create(inflater, parent);
        return holder;
    }

    @Override
    public void onBindViewHolder(MenuChannelListHolder holder, int position) {
        Channel channel = getData().get(position);
        Member member = null;

        RealmResults<Member> memberRealmResults = MembersRepository.query(new MemberById(channel.getId()));
        memberRealmResults.addChangeListener(element -> notifyDataSetChanged());
        if ( memberRealmResults.size()!=0){
            member = memberRealmResults.first();
        }
        holder.getmBinding().getRoot()
                .setOnClickListener(v -> {
                    Log.d(TAG, "onClickItem() holder");
                    if (channelItemClickListener != null) {
                        channelItemClickListener.onChannelClick(channel.getId(), channel.getDisplayName(), channel.getType());
                        ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(true);
                        setSelecteditem(holder.getAdapterPosition());
                        onChangeSelected();
                    }
                });

        if (holder.getAdapterPosition() == selecteditem) {
            ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(true);
        } else {
            ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(false);
        }
        holder.bindTo(channel, context, member);
    }

    public int getSelecteditem() {
        return selecteditem;
    }

    public void setSelecteditem(int selecteditem) {
        this.selecteditem = selecteditem;
        onChangeSelected();
        notifyDataSetChanged();
    }

    private void onChangeSelected() {
        if (selectedItemChangeListener != null) {
            selectedItemChangeListener.onChangeSelected(selecteditem);
        }
    }

    public void setSelectedItemChangeListener(MenuChannelListFragment.OnSelectedItemChangeListener selectedItemChangeListener) {
        this.selectedItemChangeListener = selectedItemChangeListener;
    }
}
