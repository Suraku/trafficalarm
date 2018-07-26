package com.suraku.trafficalarm.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.suraku.trafficalarm.Helper;
import com.suraku.trafficalarm.Logging;
import com.suraku.trafficalarm.R;
import com.suraku.trafficalarm.data.extensions.EventLevel;
import com.suraku.trafficalarm.databinding.ActivityAddressBinding;
import com.suraku.trafficalarm.models.Address;

/**
 * Add/Edit addresses
 */
public class AddressActivity extends BaseActivity
{
    private Address Model;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        // Bind the model to the view
        ActivityAddressBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_address);
        Model = new Address(Helper.getUser(this));
        binding.setModel(Model);

        // Pass bundle data
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString(getString(R.string.key_addressActivity_title));

        if (!Helper.isNullOrEmpty(title)) {
            setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_address, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_addressSave) {
            _saveAddress();
        }

        return super.onOptionsItemSelected(item);
    }

    public void SaveAddressBtn_Click(View view) {
        _saveAddress();
    }

    private void _saveAddress() {
        int i = Model.saveChanges(this);
        if (i == -2) {
            Helper.createScalableToast(this, getString(
                    R.string.value_addressActivity_completeWarning), Toast.LENGTH_LONG)
                    .show();
        } else {
            Logging.logEvent(this, getString(R.string.logging_addressActivity_addressSaved), EventLevel.LOW);
            this.finish();
        }
    }
}
