package cn.cbsd.websockettest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.net.URI;

public class ServerActivity extends Activity {

    ServerManager myWebSocketServer = new ServerManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            myWebSocketServer.Start(4545);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myWebSocketServer.Stop();

    }
}
