package com.accela.contractorcentral.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accela.contractorcentral.R;
import com.accela.contractorcentral.utils.APIHelper;
import com.accela.contractorcentral.view.RoundedImageView;

@SuppressLint("NewApi") public class EnterNewContactFragment extends Fragment {
	private int  GALLERY_INTENT_CALLED = 1;
	private int  GALLERY_KITKAT_INTENT_CALLED = 2;
	private EditText editFirstName;
	private EditText editLastName;
	private EditText editPhone;
	private TextView textNameInitials;
	private RoundedImageView imageProfile;
	private String imagePath;
	private View contentView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contentView = inflater.inflate(R.layout.activity_enter_new_contact_fragment, container, false);
		editFirstName = (EditText) contentView.findViewById(R.id.editContactFirstnameId);
		editLastName = (EditText) contentView.findViewById(R.id.editContactLastnameId);
		editPhone = (EditText) contentView.findViewById(R.id.editContactPhoneId);
		textNameInitials = (TextView) contentView.findViewById(R.id.textNameInitials);
		imageProfile = (RoundedImageView) contentView.findViewById(R.id.imageProfile);

		editPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

		LinearLayout addPhotoLayout = (LinearLayout) contentView.findViewById(R.id.addPhoto);
		addPhotoLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT < 19){
					Intent intent = new Intent(); 
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intent.createChooser(intent, getResources().getString(R.string.select_picture)), GALLERY_INTENT_CALLED);
				} else {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
					startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
				}

			}
		});

		return contentView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Uri originalUri;

		if (data != null) {
			originalUri = data.getData();

			if (requestCode == GALLERY_INTENT_CALLED && resultCode == Activity.RESULT_OK && data.getData() != null) {
				originalUri = data.getData();
				imagePath = APIHelper.getPath(getActivity(),originalUri);
				imageProfile.setImagePath(imagePath);
				textNameInitials.setVisibility(View.INVISIBLE);
			} else if (requestCode == GALLERY_KITKAT_INTENT_CALLED && resultCode == Activity.RESULT_OK && data.getData() != null) {
				imagePath = APIHelper.getPath(getActivity(),originalUri);  
				final int takeFlags = data.getFlags()
						& (Intent.FLAG_GRANT_READ_URI_PERMISSION
								| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				// Check for the freshest data.
				getActivity().getContentResolver().takePersistableUriPermission(originalUri, takeFlags);
				imageProfile.setImagePath(imagePath);
				textNameInitials.setVisibility(View.INVISIBLE);
			}
		}
	}

	public String getContactFirstName(){
		return editFirstName.getText().toString();
	}

	public String getContactLastName(){
		return editLastName.getText().toString();
	}

	public String getContactPhone(){
		return editPhone.getText().toString();
	}


	public String getImagePath() {
		return this.imagePath;
	}
}
