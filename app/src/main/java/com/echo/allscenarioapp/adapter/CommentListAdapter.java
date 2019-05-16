package com.echo.allscenarioapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.model.CommentListModel;
import com.echo.allscenarioapp.model.FriendsListModel;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by EchoIT on 11/29/2018.
 */

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.MyViewHolder> {

    Context context;
    private LayoutInflater inflater = null;
    public ArrayList<CommentListModel> objList = null;
    private View.OnClickListener clickListener=null;

    public CommentListAdapter(Context context, View.OnClickListener clickListener) {
        this.context = context;
        objList = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.clickListener =clickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.raw_comments, viewGroup, false);
        Utils.applyFontFace(context, itemView);
        return new MyViewHolder(itemView);
    }

    public void addData(ArrayList<CommentListModel> mobjList) {
        objList = new ArrayList<>();
        if (mobjList != null && mobjList.size() > 0)
            objList.addAll(mobjList);

        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.imgMore.setTag(position);
        holder.imgMore.setOnClickListener(clickListener);


        holder.img_profile_picture.setActualImageResource(R.drawable.avatar);
        if (objList.get(position).image != null && !objList.get(position).image.isEmpty()) {
            holder.img_profile_picture.setImageURI(Uri.parse(objList.get(position).image));
        }

        holder.tv_username.setText(objList.get(position).fullname);
        holder.tvComments.setText(objList.get(position).comment);
        holder.tvDate.setText(objList.get(position).created!=null && !objList.get(position).created.isEmpty() ? Utils.getAbbreviatedTimeSpanForReview(Utils.getStringToMillsTime(objList.get(position).created)):"");


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
        @BindView(R.id.img_profile_picture)
        SimpleDraweeView img_profile_picture;
        @BindView(R.id.tv_username)
        TextView tv_username;
        @BindView(R.id.tvComments)
        TextView tvComments;
        @BindView(R.id.tvDate)
        TextView tvDate;
        @BindView(R.id.imgMore)
        ImageView imgMore;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
