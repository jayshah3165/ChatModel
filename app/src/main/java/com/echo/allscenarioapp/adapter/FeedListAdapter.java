package com.echo.allscenarioapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.model.FeedListModel;
import com.echo.allscenarioapp.model.FriendsListModel;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by EchoIT on 11/29/2018.
 */

public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.MyViewHolder> {

    Context context;
    private LayoutInflater inflater = null;
    public ArrayList<FeedListModel> objList = null;
    private View.OnClickListener clickListener=null;

    public FeedListAdapter(Context context, View.OnClickListener clickListener) {
        this.context = context;
        objList = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.clickListener =clickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_feed, viewGroup, false);
        Utils.applyFontFace(context, itemView);
        return new MyViewHolder(itemView);
    }

    public void addData(ArrayList<FeedListModel> mobjList) {
        objList = new ArrayList<>();
        if (mobjList != null && mobjList.size() > 0)
            objList.addAll(mobjList);

        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        holder.imgMore.setTag(position);
        holder.imgMore.setOnClickListener(clickListener);
        holder.imgLikeUser.setTag(position);
        holder.imgLikeUser.setOnClickListener(clickListener);
        holder.rrCommentView.setTag(position);
        holder.rrCommentView.setOnClickListener(clickListener);
        holder.rrComment.setTag(position);
        holder.rrComment.setOnClickListener(clickListener);
        holder.play_image.setTag(position);
        holder.play_image.setOnClickListener(clickListener);
        holder.imgAudio.setTag(position);
        holder.imgAudio.setOnClickListener(clickListener);
        holder.img_upload_preview.setTag(position);
        holder.img_upload_preview.setOnClickListener(clickListener);
        holder.rrLikeView.setTag(position);
        holder.rrLikeView.setOnClickListener(clickListener);


        holder.img_profile_picture.setActualImageResource(R.drawable.avatar);
        if (objList.get(position).user_image != null && !objList.get(position).user_image.isEmpty()) {
            holder.img_profile_picture.setImageURI(Uri.parse(objList.get(position).user_image));
        }

        holder.img_upload_preview.setBackgroundColor(context.getResources().getColor(R.color.colorGray));
        if (objList.get(position).thumb_url != null && !objList.get(position).thumb_url.isEmpty()) {
            holder.img_upload_preview.setImageURI(Uri.parse(objList.get(position).thumb_url));
        }

        holder.play_image.setVisibility(objList.get(position).feed_type.equalsIgnoreCase("3") ? View.VISIBLE :View.GONE);
        holder.imgAudio.setVisibility(objList.get(position).feed_type.equalsIgnoreCase("5") ? View.VISIBLE : View.GONE);
        holder.rrCenterView.setVisibility(objList.get(position).feed_type.equalsIgnoreCase("1") ? View.GONE :View.VISIBLE);




        //  1=Text, 2=Image, 3=Video, 4=Checking 5=Audio
  /*      Utils.print("TYPEEEEEEEEEE_-----------------------------"+objList.get(position).feed_type+":::"+objList.get(position).thumb_url);
       if (objList.get(position).feed_type.equalsIgnoreCase("2"))
       {
           holder.img_upload_preview.setImageURI(Uri.parse(objList.get(position).thumb_url));

       }else if (objList.get(position).feed_type.equalsIgnoreCase("3")){

           holder.play_image.setVisibility(View.VISIBLE);
           holder.imgAudio.setVisibility(View.GONE);

       }else if (objList.get(position).feed_type.equalsIgnoreCase("5"))
       {
           holder.img_upload_preview.setBackgroundColor(context.getResources().getColor(R.color.colorGray));
           holder.imgAudio.setVisibility(View.VISIBLE);
       }else if (objList.get(position).feed_type.equalsIgnoreCase("1"))
       {
           holder.rrCenterView.setVisibility(View.GONE);
           holder.imgAudio.setVisibility(View.GONE);
       }else if (objList.get(position).feed_type.equalsIgnoreCase("4")){
           holder.play_image.setVisibility(View.GONE);
           holder.img_upload_preview.setBackgroundColor(context.getResources().getColor(R.color.colorGray));
           holder.imgAudio.setVisibility(View.GONE);
       }
*/
       /* holder.img_upload_preview.setActualImageResource(R.drawable.avatar);
        if (objList.get(position).upload_img != null && !objList.get(position).upload_img.isEmpty()) {
            holder.img_upload_preview.setImageURI(Uri.parse(objList.get(position).upload_img));
        }*/

        holder.txtUserName.setText(objList.get(position).title);
        holder.txtCaption.setText(objList.get(position).message);

        holder.txtFeedDate.setText(objList.get(position).created!=null && !objList.get(position).created.isEmpty() ? Utils.getAbbreviatedTimeSpanForReview(Utils.getStringToMillsTime(objList.get(position).created)):"");
        holder.txtTotalLike.setText(objList.get(position).number_of_likes);
        holder.txtTotalComment.setText(objList.get(position).number_of_comments);

        if (objList.get(position).is_like.equalsIgnoreCase("1")) {
            holder.imgLikeUser.setImageResource(R.drawable.ic_like_thumb_filled);
            holder.txtLike.setTextColor(context.getResources().getColor(R.color.green_color));
        }
        else {
            holder.imgLikeUser.setImageResource(R.drawable.ic_like_thumb);
            holder.txtLike.setTextColor(context.getResources().getColor(R.color.gray_color));
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
        @BindView(R.id.img_profile_picture)
        SimpleDraweeView img_profile_picture;
        @BindView(R.id.txtUserName)
        TextView txtUserName;
        @BindView(R.id.txtFeedDate)
        TextView txtFeedDate;
        @BindView(R.id.txtCaption)
        TextView txtCaption;
        @BindView(R.id.view_more)
        TextView view_more;
        @BindView(R.id.bg_image)
        ImageView bg_image;
        @BindView(R.id.img_upload_preview)
        SimpleDraweeView img_upload_preview;
        @BindView(R.id.play_image)
        ImageView play_image;
        @BindView(R.id.imgLikeUser)
        ImageView imgLikeUser;
        @BindView(R.id.imgAudio)
        ImageView imgAudio;
        @BindView(R.id.txtTotalLike)
        TextView txtTotalLike;
        @BindView(R.id.txtTotalComment)
        TextView txtTotalComment;
        @BindView(R.id.txtLike)
        TextView txtLike;
        @BindView(R.id.imgMore)
        ImageView imgMore;
        @BindView(R.id.rrLike)
        RelativeLayout rrLike;
        @BindView(R.id.rrComment)
        RelativeLayout rrComment;
        @BindView(R.id.rrCenterView)
        RelativeLayout rrCenterView;
        @BindView(R.id.rrCommentView)
        RelativeLayout rrCommentView;
        @BindView(R.id.rrLikeView)
        RelativeLayout rrLikeView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
