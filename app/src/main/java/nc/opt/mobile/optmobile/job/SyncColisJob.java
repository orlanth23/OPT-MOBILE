package nc.opt.mobile.optmobile.job;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

import nc.opt.mobile.optmobile.service.SyncColisService;

/**
 * Created by 2761oli on 10/10/2017.
 */

public class SyncColisJob extends Job {
    public static final String TAG = "sync_colis_job";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {

        SyncColisService.launchSynchroFromScheduler(getContext());
        return Result.SUCCESS;
    }

    /**
     * Schedule a periodic job wich will be launch every 30 minutes.
     */
    public static void scheduleJob() {
        new JobRequest.Builder(SyncColisJob.TAG)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setUpdateCurrent(true)
                .setPeriodic(TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(5))
                .build()
                .schedule();
    }
}