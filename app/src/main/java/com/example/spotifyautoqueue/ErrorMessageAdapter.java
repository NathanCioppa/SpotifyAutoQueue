package com.example.spotifyautoqueue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// Adapter for recycler view which displays messages in the error log
public class ErrorMessageAdapter extends RecyclerView.Adapter<ErrorMessageAdapter.ThisViewHolder>{
    Context context;
    ArrayList<ErrorMessage> errors;

    public ErrorMessageAdapter(Context context, ArrayList<ErrorMessage> errors) {
        this.context = context;
        this.errors = errors;
    }

    @NonNull
    @Override
    public ErrorMessageAdapter.ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.error_message_recycler_object, parent, false);

        return new ErrorMessageAdapter.ThisViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position) {
        ArrayList<ErrorMessage> logErrors = ErrorLogActivity.errors;

        holder.tagTime.setText(logErrors.get(position).getTag()+" "+logErrors.get(position).getTime());
        holder.message.setText(logErrors.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return ErrorLogActivity.errors.size();
    }

    public static class ThisViewHolder extends RecyclerView.ViewHolder {

        TextView tagTime;
        TextView message;
        public ThisViewHolder(@NonNull View itemView) {
            super(itemView);

            tagTime = itemView.findViewById(R.id.errorTimeAndTag);
            message = itemView.findViewById(R.id.errorMessageView);
        }
    }
}
