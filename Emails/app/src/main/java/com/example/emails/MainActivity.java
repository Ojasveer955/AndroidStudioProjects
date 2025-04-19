package com.example.emails;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    EditText mail_to, mail_subject, message;
    Button send_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mail_to = findViewById(R.id.Mail_To);
        mail_subject = findViewById(R.id.Email_subject);
        message = findViewById(R.id.Message);
        send_mail = findViewById(R.id.Send_Mail);

        send_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });
    }

    private void sendEmail(){
        String recipient = mail_to.getText().toString().trim();
        String subject =  mail_subject.getText().toString().trim();
        String msg = message.getText().toString().trim();

        if (recipient.isEmpty()){
            Toast.makeText(this, "Please enter recipient email.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Intent to send mail via any email app
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822"); //TO ensure only email app is sent to intent to.

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, msg);

        try {
            startActivity(Intent.createChooser(intent, "Choose an email client"));
        }

        catch (Exception e){
            Toast.makeText(this, "No email apps detected.", Toast.LENGTH_SHORT).show();
        }
    }

}
