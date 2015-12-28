package be.itstudents.tom.android.cinema.datafetcher;

public abstract class Loader extends Thread {
    private LoaderEvent listener;
    private boolean finished = false;


    public void setLoaderEvent(LoaderEvent listener) {
        this.listener = listener;
    }

    public void callOnFinish() {
        finished = true;
        if (listener != null)
            listener.onFinish();
    }

    public void callOnCancel() {
        if (listener != null)
            listener.onCancel();
    }

    public void callOnGoBackground() {
        if (listener != null)

            listener.onGoBackground();
    }

    public void callOnProgress(int progress, int total) {
        if (listener != null)
            listener.onProgress(progress, total);
    }

    public void callOnStatus(String title, String desc) {
        if (listener != null)
            listener.onStatus(title, desc);
    }

    public boolean isFinished() {
        return finished;
    }

    public interface LoaderEvent {
        void onFinish();

        void onCancel();

        void onProgress(int progress, int total);

        void onStatus(String title, String desc);

        void onGoBackground();
    }
}
