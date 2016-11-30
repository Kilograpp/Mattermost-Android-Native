package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.InviteMemberItemBinding;
import com.kilogramm.mattermost.model.fromnet.InviteObject;

/**
 * Created by kepar on 17.11.16.
 */

public class InviteUserAdapter extends HeaderFooterRecyclerArrayAdapter<InviteUserAdapter.ViewHolder, InviteObject> {

    private Context context;
    private boolean shouldCheckNullFields = false;

    private TextWatcher emailTextWatcher;
    private TextWatcher firstNameTextWatcher;
    private TextWatcher lastNameTextWatcher;

    public InviteUserAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateGenericViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.create(LayoutInflater.from(context), parent);
    }

    @Override
    public void onBindGenericViewHolder(ViewHolder holder, int position) {
        InviteObject item = getItem(position);
        holder.binding.memberNumberLabel.setText(String.format("%s%d", context.getString(R.string.member), (position + 1)));
        if (position == 0 && getDataCount() <= 1) {
            holder.binding.delete.setVisibility(View.GONE);
        } else {
            holder.binding.delete.setVisibility(View.VISIBLE);
            holder.binding.delete.setOnClickListener(v -> removeItem(position));
        }

        holder.binding.editEmail.setText(item.getEmail());
        holder.binding.editFirstName.setText(item.getFirstName());
        holder.binding.editLastName.setText(item.getLastName());

        setTextChangeListeners(holder);

        checkEmailField(holder, item);
    }

    @Override
    public void removeItem(int position) {
        super.removeItem(position);
        if(getDataCount() > 1) {
            notifyItemRangeChanged(position, getData().size() - 1);
        } else if (getDataCount() == 1){
            notifyItemChanged(position);
        }
    }

    public int isAllValid() {
        notifyDataSetChanged();
        for (InviteObject inviteObject : getData()) {
            String email = inviteObject.getEmail();
            if (email == null || email.length() == 0
                    || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                shouldCheckNullFields = true;
                return getData().indexOf(inviteObject);
            }
        }
        return -1;
    }

    private void setTextChangeListeners(ViewHolder holder) {
        holder.binding.editEmail.removeTextChangedListener(emailTextWatcher);
        holder.binding.editFirstName.removeTextChangedListener(firstNameTextWatcher);
        holder.binding.editLastName.removeTextChangedListener(lastNameTextWatcher);

        emailTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getData().get(holder.getAdapterPosition()).setEmail(s.toString());
            }
        };
        firstNameTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getData().get(holder.getAdapterPosition()).setFirstName(s.toString());
            }
        };
        lastNameTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getData().get(holder.getAdapterPosition()).setLastName(s.toString());
            }
        };

        holder.binding.editEmail.addTextChangedListener(emailTextWatcher);
        holder.binding.editFirstName.addTextChangedListener(firstNameTextWatcher);
        holder.binding.editLastName.addTextChangedListener(lastNameTextWatcher);

        holder.binding.editEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && holder.getAdapterPosition() >= 0) {
                checkEmailField(holder, getData().get(holder.getAdapterPosition()));
            }
        });
    }

    public void setShouldCheckNullFields(boolean shouldCheckNullFields) {
        this.shouldCheckNullFields = shouldCheckNullFields;
    }

    private void checkEmailField(ViewHolder holder, InviteObject item) {
        String email = item.getEmail();
        if (email != null && email.length() > 0) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(item.getEmail()).matches()) {
                holder.binding.inputEmail.setErrorEnabled(true);
                holder.binding.inputEmail.setError(context.getString(R.string.invalid_email));
            } else {
                holder.binding.inputEmail.setErrorEnabled(false);
            }
        } else if (shouldCheckNullFields) {
            holder.binding.inputEmail.setErrorEnabled(true);
            holder.binding.inputEmail.setError(context.getString(R.string.invalid_email));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private InviteMemberItemBinding binding;

        public ViewHolder(InviteMemberItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            InviteMemberItemBinding binding = InviteMemberItemBinding.inflate(inflater, parent, false);
            return new ViewHolder(binding);
        }
    }
}