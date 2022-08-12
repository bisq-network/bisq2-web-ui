package bisq.web.ui.easy;

public interface IBisqEasyView {
    void stateChanged();


    Runnable pushCallBack(Runnable command);
}
