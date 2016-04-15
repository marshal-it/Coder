package com.tony.coder.im.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.tony.coder.R;
import com.tony.coder.im.ui.activity.ChatActivity;
import com.tony.coder.im.ui.adapter.MessageRecentAdapter;
import com.tony.coder.im.view.dialog.DialogTips;
import com.tony.coder.im.view.xlist.XListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.db.BmobDB;

/**
 * 项目名称：Coder
 * 类描述：
 * 创建人：tonycheng
 * 创建时间：2016/3/31 20:59
 * 邮箱：tonycheng93@outlook.com
 * 修改人：
 * 修改时间：
 * 修改备注：
 */
public class ChatFragment extends BaseFragment implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    View mView;
    //    @Bind(R.id.et_msg_search)
//    ClearEditText mClearEditText;
    @Bind(R.id.list)
    XListView mXListView;

    private MessageRecentAdapter mAdapter;
    private List<BmobRecent> mDatas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDatas = BmobDB.create(getActivity()).queryRecents();
        initView();
    }

    private void initView() {
        initTopBarForOnlyTitle("会话");
        mXListView.setOnItemClickListener(this);
        mXListView.setOnItemLongClickListener(this);
        mAdapter = new MessageRecentAdapter(getActivity(), R.layout.item_conversation, BmobDB.create(getActivity()).queryRecents());
        mXListView.setAdapter(mAdapter);


//        mClearEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
////                mAdapter.getFilter().filter(charSequence);
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
    }

    /**
     * 删除会话
     *
     * @param recent
     */
    public void deleteRecent(BmobRecent recent) {
        mAdapter.remove(recent);
        BmobDB.create(getActivity()).deleteRecent(recent.getTargetid());
        BmobDB.create(getActivity()).deleteMessages(recent.getTargetid());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BmobRecent bmobRecent = mAdapter.getItem(position);
        //重置未读消息
        BmobDB.create(getActivity()).resetUnread(bmobRecent.getTargetid());
        //组装聊天对象
        BmobChatUser user = new BmobChatUser();
        user.setAvatar(bmobRecent.getAvatar());
        user.setNick(bmobRecent.getNick());
        user.setUsername(bmobRecent.getUserName());
        user.setObjectId(bmobRecent.getTargetid());
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("user", user);
        startAnimActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        BmobRecent recent = mAdapter.getItem(position);
        showDeleteDialog(recent);
        return false;
    }

    public void showDeleteDialog(final BmobRecent bmobRecent) {
        DialogTips dialogTips = new DialogTips(getActivity(), bmobRecent.getUserName(), "删除会话",
                "确定", true, true);
        //设置成功事件
        dialogTips.setOnSuccessListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteRecent(bmobRecent);
            }
        });
        //显示对话框
        dialogTips.show();
        dialogTips = null;
    }

    private boolean hidden;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (!hidden) {
            refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden) {
            refresh();
        }
    }

    public void refresh() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter = new MessageRecentAdapter(getActivity(), R.layout.item_conversation, BmobDB.create(getActivity()).queryRecents());
                    mXListView.setAdapter(mAdapter);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}