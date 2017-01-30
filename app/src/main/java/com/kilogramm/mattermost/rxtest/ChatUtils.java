package com.kilogramm.mattermost.rxtest;

import android.util.Log;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ServerMethod;

import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 23.01.2017.
 */

public class ChatUtils  {

    private static final String TAG = "ChatUtils";


    public static String getInfoChannelNotDirect(String mChannelId) {
        RealmResults<ExtraInfo> rExtraInfo =
                ExtroInfoRepository.query(new ExtroInfoRepository.ExtroInfoByIdSpecification(mChannelId));
        if (rExtraInfo.size() != 0) {
            return String.format("%s members", rExtraInfo.first().getMember_count());
        } else return "";
    }

    public static String getInfoChannelDirect(String mChannelId) {
        RealmResults<Channel> rChannel =
                ChannelRepository.query(new ChannelRepository.ChannelByIdSpecification(mChannelId));
        if (rChannel.size() == 0) {
            return UserStatus.OFFLINE;
        }

        String userId = rChannel.first()
                .getName()
                .replace(MattermostPreference.getInstance().getMyUserId(), "");
        userId = userId.replace("__", "");

        RealmResults<UserStatus> usersStatusList = UserStatusRepository.query(new UserStatusRepository.UserStatusByIdSpecification(userId));

        if (usersStatusList.size() == 0) {
            return UserStatus.OFFLINE;
        }
        return usersStatusList.first().getStatus();
    }

    public static boolean isDirectChannel(String mChannelType) {
        return mChannelType != null && mChannelType.equals("D");
    }

    public static String getDirectUserId(String mChannelId) {
        RealmResults<Channel> rChannel = ChannelRepository.query(new ChannelRepository.ChannelByIdSpecification(mChannelId));
        if (rChannel.size() == 0) {
            return null;
        }
        String userId = rChannel.first()
                .getName()
                .replace(MattermostPreference.getInstance().getMyUserId(), "");
        return userId.replace("__", "");
    }

    public static void createObservablesList(List<Observable<List<FileInfo>>> observables, Posts posts,
                                       String mTeamId, String mChannelId) {
        for (Map.Entry<String, Post> entry : posts.getPosts().entrySet()) {
            if (entry.getValue().getFilenames().size() > 0) {
                observables.add(ServerMethod.getInstance()
                        .getFileInfo(mTeamId, mChannelId, entry.getValue().getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()));
            }
        }
    }

    public static void saveFileInfo(List<FileInfo> fileInfos) {
        if (fileInfos == null) return;
        for (FileInfo fileInfo : fileInfos) {
            Log.d(TAG, "saveFileInfo() called with: fileInfo = [" + fileInfo.getId() + "]");
            FileInfoRepository.getInstance().add(fileInfo);
        }
    }

    public static void savePosts(List<Posts> list) {
        Log.d(TAG, "savePosts() called with: list size = [" + list.size() + "]");
        for (Posts posts : list) {
            PostRepository.prepareAndAdd(posts);
        }
    }
    public static void savePosts(Posts posts){
        PostRepository.prepareAndAdd(posts);
    }


    public static String getLastMessageId(String channelId) {
        RealmResults<Post> realmList = PostRepository.query(new PostByChannelId(channelId));
        if (realmList.size() != 0) {
            Log.d(TAG, "lastmessage " + realmList.get(0).getMessage());
            return realmList.get(0).getId();
        } else {
            return null;
        }
    }

    public static String getFirstMessageId(String channelId) {
        RealmResults<Post> realmList = PostRepository.query(new PostByChannelId(channelId));
        if (realmList.size() != 0) {
            Log.d(TAG, "firstmessage " + realmList.get(realmList.size() - 1).getMessage());
            return realmList.get(realmList.size() - 1).getId();
        } else {
            return null;
        }
    }

    public static void clearCurrentChatPost(String mChannelId) {
        PostRepository.remove(new PostByChannelId(mChannelId));
    }

    public static void mergePosts(Posts posts, String channelId) {
        PostRepository.merge(posts.getPosts().values(), new PostByChannelId(channelId));
    }

    public static void removePost(Post post) {
        PostRepository.remove(post);
    }

    public static void setPostUpdateAt(String id, Long updateAt) {
        PostRepository.setUpdateAt(id, updateAt);
    }


}
