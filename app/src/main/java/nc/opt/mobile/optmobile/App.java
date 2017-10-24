package nc.opt.mobile.optmobile;

import android.app.Application;

import com.evernote.android.job.JobManager;

import nc.opt.mobile.optmobile.job.SyncColisJob;
import nc.opt.mobile.optmobile.job.SyncColisJobCreator;
import nc.opt.mobile.optmobile.utils.RequestQueueSingleton;

/**
 * Created by 2761oli on 10/10/2017.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Première instanciation du singleton
        RequestQueueSingleton.getInstance(getApplicationContext());

        JobManager.create(this).addJobCreator(new SyncColisJobCreator());

        // Plannification d'un job
        SyncColisJob.scheduleJob();

        // Lancement d'une synchro dès le début du programme
        SyncColisJob.launchImmediateJob();
    }
}
