package com.echo.allscenarioapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.activity.OneToOneChatActivity;
import com.echo.allscenarioapp.model.ChatDetailListModel;
import com.echo.allscenarioapp.utils.Const;
import com.echo.allscenarioapp.utils.Pref;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChatDetailListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    private LayoutInflater inflater = null;
    public ArrayList<ChatDetailListModel> objList = null;
    private View.OnClickListener clickListener = null;

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_CONTENT = 1;


    public ChatDetailListAdapter(Context context, View.OnClickListener clickListener) {
        this.context = context;
        objList = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.clickListener = clickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_header_of_chatdate, parent, false);
            Utils.applyFontFace(context, itemView);
            return new ChatDetailListAdapter.MyHeaderViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_chat_details_list, parent, false);
            Utils.applyFontFace(context, itemView);
            return new ChatDetailListAdapter.MyViewHolder(itemView);
        }

    }

    @Override
    public int getItemCount() {
        return objList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (objList.get(position).isHeader == 1)
            return TYPE_HEADER;
        return TYPE_CONTENT;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public void addData(ArrayList<ChatDetailListModel> mobjList) {

        objList = new ArrayList<>();
        objList.addAll(mobjList);
        this.notifyDataSetChanged();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txtUserName)
        TextView txtUserName;
        @BindView(R.id.txtUsermsg)
        TextView txtUsermsg;
        @BindView(R.id.txtMsgTime)
        TextView txtMsgTime;
        @BindView(R.id.txtTitle)
        TextView txtTitle;
        @BindView(R.id.imgSendimg)
        SimpleDraweeView imgSendimg;
        @BindView(R.id.row)
        RelativeLayout row;
        @BindView(R.id.cardview)
        CardView cardview;
        @BindView(R.id.imgAudio)
        ImageView imgAudio;
        @BindView(R.id.rlMain)
        RelativeLayout rlMain;
        @BindView(R.id.imgplay)
        ImageView imgplay;
        @BindView(R.id.imgFile)
        ImageView imgFile;
        @BindView(R.id.imgProfilePic)
        SimpleDraweeView imgProfilePic;
        @BindView(R.id.rrFriendsMsg)
        RelativeLayout rrFriendsMsg;
        @BindView(R.id.rrTitle)
        RelativeLayout rrTitle;
        @BindView(R.id.llMain)
        RelativeLayout llMain;




        @BindView(R.id.txtUserNameMy)
        TextView txtUserNameMy;
        @BindView(R.id.txtMsgTimeMy)
        TextView txtMsgTimeMy;
        @BindView(R.id.txtUsermsgMy)
        TextView txtUsermsgMy;
        @BindView(R.id.imgSendimgMy)
        SimpleDraweeView imgSendimgMy;

        @BindView(R.id.cardviewMy)
        CardView cardviewMy;
        @BindView(R.id.imgAudioMy)
        ImageView imgAudioMy;

        @BindView(R.id.imgFileMy)
        ImageView imgFileMy;
        @BindView(R.id.rlMainMy)
        RelativeLayout rlMainMy;
        @BindView(R.id.imgplayMy)
        ImageView imgplayMy;
        @BindView(R.id.imgDoubleTickMy)
        ImageView imgDoubleTickMy;
        @BindView(R.id.rrMyMsg)
        RelativeLayout rrMyMsg;




        public MyViewHolder(View convertView) {
            super(convertView);
            ButterKnife.bind(this, convertView);

        }
    }

    public class MyHeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtDate)
        TextView txtDate;




        public MyHeaderViewHolder(View convertView) {
            super(convertView);
            ButterKnife.bind(this, convertView);

        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder newholder,final int position) {
        // TODO Auto-generated method stub

        if (newholder instanceof MyViewHolder) {
            MyViewHolder holder = ((MyViewHolder) newholder);
            holder.row.setTag(position);
            holder.row.setOnClickListener(clickListener);

            holder.imgAudio.setTag(position);
            holder.imgAudio.setOnClickListener(clickListener);

            holder.imgplay.setTag(position);
            holder.imgplay.setOnClickListener(clickListener);


            holder.imgplayMy.setTag(position);
            holder.imgplayMy.setOnClickListener(clickListener);


            holder.imgAudioMy.setTag(position);
            holder.imgAudioMy.setOnClickListener(clickListener);


            holder.cardviewMy.setTag(position);
            holder.cardviewMy.setOnClickListener(clickListener);

            holder.imgplayMy.setTag(position);
            holder.imgplayMy.setOnClickListener(clickListener);


            //(1=text message 2=video 3=audio 4=image)

            if (objList.get(position).type.equalsIgnoreCase("1")) {
                holder.txtUsermsg.setVisibility(View.VISIBLE);
                holder.txtUsermsgMy.setVisibility(View.VISIBLE);
                holder.cardview.setVisibility(View.GONE);
                holder.cardviewMy.setVisibility(View.GONE);
                holder.imgAudio.setVisibility(View.GONE);
                holder.imgAudioMy.setVisibility(View.GONE);
                holder.imgplay.setVisibility(View.GONE);
                holder.imgplayMy.setVisibility(View.GONE);
                holder.rrTitle.setVisibility(View.GONE);
                holder.imgSendimg.setVisibility(View.VISIBLE);
                holder.imgSendimgMy.setVisibility(View.VISIBLE);

            } else if (objList.get(position).type.equalsIgnoreCase("4")) {
                holder.txtUsermsg.setVisibility(View.GONE);
                holder.txtUsermsgMy.setVisibility(View.GONE);
                holder.cardview.setVisibility(View.VISIBLE);
                holder.cardviewMy.setVisibility(View.VISIBLE);
                holder.imgAudio.setVisibility(View.GONE);
                holder.imgAudioMy.setVisibility(View.GONE);
                holder.imgplay.setVisibility(View.GONE);
                holder.imgplayMy.setVisibility(View.GONE);
                holder.rrTitle.setVisibility(View.GONE);
                holder.imgSendimg.setVisibility(View.VISIBLE);
                holder.imgSendimgMy.setVisibility(View.VISIBLE);

            } else if (objList.get(position).type.equalsIgnoreCase("2")) {
                holder.txtUsermsg.setVisibility(View.GONE);
                holder.txtUsermsgMy.setVisibility(View.GONE);
                holder.cardview.setVisibility(View.VISIBLE);
                holder.cardviewMy.setVisibility(View.VISIBLE);
                holder.imgAudio.setVisibility(View.GONE);
                holder.imgAudioMy.setVisibility(View.GONE);
                holder.imgplay.setVisibility(View.VISIBLE);
                holder.imgplayMy.setVisibility(View.VISIBLE);
                holder.rrTitle.setVisibility(View.GONE);
                holder.imgSendimg.setVisibility(View.VISIBLE);
                holder.imgSendimgMy.setVisibility(View.VISIBLE);

            } else if (objList.get(position).type.equalsIgnoreCase("3")) {
                holder.txtUsermsg.setVisibility(View.GONE);
                holder.txtUsermsgMy.setVisibility(View.GONE);
                holder.cardview.setVisibility(View.GONE);
                holder.cardviewMy.setVisibility(View.GONE);
                holder.imgAudio.setVisibility(View.VISIBLE);
                holder.imgAudioMy.setVisibility(View.VISIBLE);
                holder.imgplay.setVisibility(View.GONE);
                holder.imgplayMy.setVisibility(View.GONE);
                holder.rrTitle.setVisibility(View.GONE);
                holder.imgSendimg.setVisibility(View.VISIBLE);
                holder.imgSendimgMy.setVisibility(View.VISIBLE);


            }else if (objList.get(position).type.equalsIgnoreCase("5")) {
                holder.txtUsermsg.setVisibility(View.GONE);
                holder.txtUsermsgMy.setVisibility(View.GONE);
                holder.cardview.setVisibility(View.VISIBLE);
                holder.cardviewMy.setVisibility(View.VISIBLE);
                holder.imgAudio.setVisibility(View.GONE);
                holder.imgAudioMy.setVisibility(View.GONE);
                holder.imgplay.setVisibility(View.GONE);
                holder.imgplayMy.setVisibility(View.GONE);
                holder.imgSendimg.setVisibility(View.GONE);
                holder.imgSendimgMy.setVisibility(View.GONE);
                holder.imgFile.setVisibility(View.VISIBLE);
                holder.imgFileMy.setVisibility(View.VISIBLE);
                holder.rrTitle.setVisibility(View.GONE);


            }else if  (objList.get(position).type.equalsIgnoreCase("6")) {
                holder.txtUsermsg.setVisibility(View.GONE);
                holder.txtUsermsgMy.setVisibility(View.GONE);
                holder.cardview.setVisibility(View.GONE);
                holder.cardviewMy.setVisibility(View.GONE);
                holder.imgAudio.setVisibility(View.GONE);
                holder.imgAudioMy.setVisibility(View.GONE);
                holder.imgplay.setVisibility(View.GONE);
                holder.imgplayMy.setVisibility(View.GONE);
                holder.imgFile.setVisibility(View.GONE);
                holder.imgFileMy.setVisibility(View.GONE);
                holder.rrTitle.setVisibility(View.VISIBLE);
                holder.llMain.setVisibility(View.GONE);
                holder.txtTitle.setText(objList.get(position).message);
                holder.imgSendimg.setVisibility(View.VISIBLE);
                holder.imgSendimgMy.setVisibility(View.VISIBLE);
            }
          //  holder.txtTitle.setText(objList.get(position).message);


            //Preview Image
            if (objList.get(position).type.equalsIgnoreCase("4")) {

                holder.cardview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.setDialogImage(context, objList.get(position).thumb_url);
                    }
                });
            }
            if (objList.get(position).type.equalsIgnoreCase("4")){

                holder.cardviewMy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.setDialogImage(context,objList.get(position).thumb_url);
                    }
                });
            }

            if (objList.get(position).type.equalsIgnoreCase("5")){

                holder.imgFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iHelp = new Intent(Intent.ACTION_VIEW);
                        iHelp.setData(Uri.parse(objList.get(position).media_url));
                        ((Activity) context).startActivity(iHelp);
                    }
                });
            }
            if (objList.get(position).type.equalsIgnoreCase("5")){

                holder.imgFileMy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iHelp = new Intent(Intent.ACTION_VIEW);
                        iHelp.setData(Uri.parse(objList.get(position).media_url));
                        ((Activity) context).startActivity(iHelp);
                    }
                });
            }



            if(Pref.getStringValue(context, Const.PREF_USERID, "").equalsIgnoreCase(objList.get(position).user_id)) {
                holder.rrMyMsg.setVisibility(View.VISIBLE);
                holder.rrFriendsMsg.setVisibility(View.GONE);
                holder.txtUserNameMy.setText(objList.get(position).fullname);
                holder.txtMsgTimeMy.setText(Utils.convertDateStringToString(objList.get(position).created,"yyyy-MM-dd HH:mm:ss","HH:mm a"));

                holder.txtUsermsgMy.setText(objList.get(position).message);

                holder.imgDoubleTickMy.setVisibility(objList.get(position).is_read == 1?View.VISIBLE:View.GONE);
                holder.imgProfilePic.setVisibility(View.GONE);
                holder.imgSendimgMy.setActualImageResource(R.drawable.avatar);


                if (objList.get(position).type.equalsIgnoreCase("2")) {
                    if (objList.get(position).thumb_url != null && !objList.get(position).thumb_url.equalsIgnoreCase("") && objList.get(position).media_url.startsWith("http")) {
                        holder.imgSendimgMy.setImageURI(Uri.parse(objList.get(position).thumb_url));
                    }
                }else {
                    if (objList.get(position).media_url != null && !objList.get(position).media_url.equalsIgnoreCase("") && objList.get(position).media_url.startsWith("http")) {
                        holder.imgSendimgMy.setImageURI(Uri.parse(objList.get(position).media_url));
                    }
                }


            }else {

                //Pref id and user id are not same

                // Others layout
                holder.rrMyMsg.setVisibility(View.GONE);
                holder.rrFriendsMsg.setVisibility(View.VISIBLE);


                if (context instanceof OneToOneChatActivity) {

                    if( ((OneToOneChatActivity) context).chatListModel.is_group.equalsIgnoreCase("1")){

                        holder.imgProfilePic.setVisibility(View.VISIBLE);
                    }else {
                        holder.imgProfilePic.setVisibility(View.GONE);
                    }
                }

                holder.imgProfilePic.setActualImageResource(R.drawable.avatar);


                if (objList.get(position).image != null && !objList.get(position).image.isEmpty()) {
                    holder.imgProfilePic.setImageURI(Uri.parse(objList.get(position).image));

                }

                holder.txtUserName.setText(objList.get(position).fullname);
                holder.txtMsgTime.setText(Utils.convertDateStringToString(objList.get(position).created,"yyyy-MM-dd HH:mm:ss","HH:mm a"));
                holder.txtUsermsg.setText(objList.get(position).message);
                holder.imgSendimg.setActualImageResource(R.drawable.avatar);


                if (objList.get(position).type.equalsIgnoreCase("2")) {
                    if (objList.get(position).thumb_url != null && !objList.get(position).thumb_url.equalsIgnoreCase("") && objList.get(position).media_url.startsWith("http")) {
                        holder.imgSendimgMy.setImageURI(Uri.parse(objList.get(position).thumb_url));
                    }
                }else {
                    if (objList.get(position).media_url != null && !objList.get(position).media_url.equalsIgnoreCase("") && objList.get(position).media_url.startsWith("http")) {
                        holder.imgSendimgMy.setImageURI(Uri.parse(objList.get(position).media_url));
                    }
                }


            }



        } else if (newholder instanceof MyHeaderViewHolder) {
            MyHeaderViewHolder holder = ((MyHeaderViewHolder) newholder);

            long mills = Utils.getStringToMillsTime(objList.get(position).created);
            holder.txtDate.setText(Utils.getAbbreviatedTimeSpan(mills));



        }


    }

}