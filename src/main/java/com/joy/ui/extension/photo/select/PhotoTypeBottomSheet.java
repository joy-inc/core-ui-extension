package com.joy.ui.extension.photo.select;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.joy.ui.extension.R;
import com.joy.ui.view.bottomsheet.JBottomSheetDialog;

/**
 * Created by Daisw on 2017/11/22.
 */

public class PhotoTypeBottomSheet extends JBottomSheetDialog {

    public PhotoTypeBottomSheet(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_sheet_photo_type);
    }
}
