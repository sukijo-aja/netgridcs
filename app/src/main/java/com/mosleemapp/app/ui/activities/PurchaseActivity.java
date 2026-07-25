package com.mosleemapp.app.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.mosleemapp.app.R;
import com.mosleemapp.app.utils.app.SettingsManager;

public class PurchaseActivity extends AppCompatActivity {

    private MaterialCardView cardMonthly;
    private MaterialCardView cardAnnually;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        cardMonthly = findViewById(R.id.cardMonthly);
        cardAnnually = findViewById(R.id.cardAnnually);

        cardMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulatePurchase(getString(R.string.monthly));
            }
        });

        cardAnnually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulatePurchase(getString(R.string.annually));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void simulatePurchase(String item) {
        // Here you would implement the actual Google Play Billing flow.
        // For now, we simulate a successful purchase.
        String message = getString(R.string.purchase_simulated, item);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // Simulate success after a short delay (optional, but realistic)
        getWindow().getDecorView().postDelayed(() -> {
            Toast.makeText(PurchaseActivity.this, R.string.purchase_successful, Toast.LENGTH_LONG).show();

            // Close activity or navigate to premium content
            SettingsManager.getInstance(getApplicationContext()).setPremium(true);
            recreate();
            finish();
        }, 1500);
    }
}
