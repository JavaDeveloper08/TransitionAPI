package desmund.app.transitionapi;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    Context context;
    TextView tvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        tvLog = findViewById(R.id.tvLog);

        Intent intent = new Intent(this, TransitionBroadcastReceiver.class);
        PendingIntent pendingIntentBroadcast = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        List<ActivityTransition> transitions = getTransitionActivityList();
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        startGetBroadcast(pendingIntentBroadcast, request, "pendingIntentBroadcast");

        refresh();
    }

    private void refresh() {
        ArrayList<ActivityTransitionEventWrapper> events = Paper.book().read("activities", new ArrayList<ActivityTransitionEventWrapper>());
        if (events.size() > 0) {
            tvLog.setText("");
            for (int i = 0; i < (events.size() > 50 ? 50 : events.size()); i++) {
                tvLog.setText(events.get(i).getEventDisplayFormat() + "\n" + tvLog.getText().toString());
            }
        }
    }

    private void startGetBroadcast(PendingIntent pendingIntent, ActivityTransitionRequest request, final String type) {
        // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
        Task<Void> task = ActivityRecognition.getClient(this).requestActivityTransitionUpdates(request, pendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(context, "Waiting for Activity Transitions...", Toast.LENGTH_LONG).show();
            }
        });
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Toast.makeText(context, "oncomplete " + type, Toast.LENGTH_SHORT).show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "Error : " + e.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    int[] detectedActivity = new int[]{
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            // DetectedActivity.ON_FOOT,
            DetectedActivity.RUNNING,
            DetectedActivity.STILL,
            // DetectedActivity.TILTING,
            // DetectedActivity.UNKNOWN,
            DetectedActivity.WALKING};

    private List<ActivityTransition> getTransitionActivityList() {
        List<ActivityTransition> transitions = new ArrayList<>();
        for (int activity : detectedActivity) {
            transitions.add(
                    new ActivityTransition.Builder()
                            .setActivityType(activity)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                            .build());
            transitions.add(
                    new ActivityTransition.Builder()
                            .setActivityType(activity)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                            .build());
        }
        return transitions;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
