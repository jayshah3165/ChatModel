package com.echo.allscenarioapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by EchoIT on 11/29/2018.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {

    Context context;
    private LayoutInflater inflater = null;
    public ArrayList<ChatDetailListModel> objList = null;
    private View.OnClickListener clickListener=null;
    View.OnLongClickListener onLongClickListener = null;
    public ChatListAdapter(Context context, View.OnClickListener clickListener, View.OnLongClickListener onLongClickListener) {
        this.context = context;
        objList = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.clickListener =clickListener;
        this.onLongClickListener = onLongClickListener;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_chat_list, viewGroup, false);
        Utils.applyFontFace(context, itemView);
        return new MyViewHolder(itemView);
    }

    public void addData(ArrayList<ChatDetailListModel> mobjList) {
        objList = new ArrayList<>();
        if (mobjList != null && mobjList.size() > 0)
            objList.addAll(mobjList);

        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.row.setTag(position);
        holder.row.setOnClickListener(clickListener);
        holder.row.setOnLongClickListener(onLongClickListener);


        holder.imgProfilePic.setActualImageResource(R.drawable.avatar);
        if (objList.get(position).image != null && !objList.get(position).image.isEmpty()) {
            holder.imgProfilePic.setImageURI(Uri.parse(objList.get(position).image));
        }

        holder.txtUserName.setText(objList.get(position).fullname);
        holder.txtMasseges.setText(objList.get(position).message);
        holder.txtMsgTime.setText(objList.get(position).created!=null && !objList.get(position).created.isEmpty() ? Utils.getAbbreviatedTimeSpanForReview(Utils.getStringToMillsTime(objList.get(position).created)):"");

        if (objList.get(position).unread!=null && !objList.get(position).unread.isEmpty() && !objList.get(position).unread.equalsIgnoreCase("0")) {
            holder.txtNotificationCount.setVisibility(View.VISIBLE);
            holder.txtNotificationCount.setText(objList.get(position).unread);
        }else {
            holder.txtNotificationCount.setVisibility(View.GONE);
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return objList != null ? objList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row)
        RelativeLayout row;
        @BindView(R.id.imgProfilePic)
        SimpleDraweeView imgProfilePic;
        @BindView(R.id.txtUserName)
        TextView txtUserName;
        @BindView(R.id.txtMasseges)
        TextView txtMasseges;
        @BindView(R.id.txtNotificationCount)
        TextView txtNotificationCount;
        @BindView(R.id.txtMsgTime)
        TextView txtMsgTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
