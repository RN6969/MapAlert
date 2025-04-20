package com.example.mapalert.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapalert.activities.LiveTrackingActivity;
import com.example.mapalert.R;
import com.example.mapalert.models.Member;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private final List<Member> memberList;
    private final Context context;

    public MemberAdapter(List<Member> memberList, Context context) {
        this.memberList = memberList;
        this.context = context;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = memberList.get(position);
        holder.nameTextView.setText(member.getName());
        holder.locationTextView.setText("Last location: " + member.getLatitude() + ", " + member.getLongitude());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, LiveTrackingActivity.class);
            intent.putExtra("memberId", member.getId());
            context.startActivity(intent);
        });

        holder.optionsMenu.setOnClickListener(v -> showPopupMenu(v, member));
    }

    private void showPopupMenu(View view, Member member) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.member_options_menu);

        final int REMOVE = R.id.action_remove;
        final int VIEW_DETAILS = R.id.action_view_details;
        final int SHARE_LOCATION = R.id.action_share_location;

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == REMOVE) {
                removeMember(member.getId());
                return true;
            } else if (itemId == VIEW_DETAILS) {
                // Implement View Details logic
                return true;
            } else if (itemId == SHARE_LOCATION) {
                shareLocationWithMember(member.getId());
                return true;
            }
            return false;
        });
        popup.show();
    }


    private void removeMember(String memberId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        assert currentUserId != null;
        DatabaseReference membersRef = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUserId).child("members");

        membersRef.child(memberId).removeValue();
    }

    private void shareLocationWithMember(String memberId) {
        // Implement live location sharing logic here
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, locationTextView;
        View optionsMenu;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.member_name);
            locationTextView = itemView.findViewById(R.id.member_location);
            optionsMenu = itemView.findViewById(R.id.member_options);
        }
    }
}
