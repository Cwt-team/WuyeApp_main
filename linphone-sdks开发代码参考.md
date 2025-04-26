SipPhone.java : 初始化

import org.linphone.core.Account;
import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Config;
import org.linphone.core.Core;
import org.linphone.core.CoreListener;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;

public class SipPhone extends IPhone {
    Factory factory;
    Core core;
    AuthInfo user;
    AccountParams accountParams;
    Call currentCall;
	//初始化Factory, 在APP启动时调用.
    public static void loadSipLibs(){
        Factory.instance();
    }
    
    void initSip(Activity activity){
        Logger.i(TAG, "initSip");
        factory = Factory.instance();
        core = factory.createCore(null, null, activity);
        core.addListener(coreListener);

        //配置视频通话
        core.enableVideoCapture(true);
        core.enableVideoDisplay(true);
        core.getVideoActivationPolicy().setAutomaticallyAccept(true);

		//音频部分, 这里增加了一个遍历, 用于设置指定的音频格式.
        //h264, no VP8 fixed outgoing call no video.
        PayloadType[] payloads = core.getVideoPayloadTypes();
        for(int i = 0; i < payloads.length; i ++){
            //Payload:null, VP8/90000/0, A VP8 video encoder using libvpx library., VP8
            //Payload:profile-level-id=42801F, H264/90000/0, A H264 encoder based on MediaCodec API., H264
            PayloadType pt = payloads[i];
            //判断是否指定的音频格式.
            boolean goodPayload = PREFER_PAYLOAD.equals(pt.getMimeType());
            pt.enable(goodPayload);
        }
        //https://github.com/BelledonneCommunications/linphone-android/issues/1153
        //https://blog.csdn.net/AdrianAndroid/article/details/70048040
        //do not working
        //H264Helper.setH264Mode(H264Helper.MODE_AUTO, core);

        //回声消除, 与音频增益.
        //Logger.d(TAG, "initSip Cancellation=" + core.echoCancellationEnabled());
        Logger.d(TAG, "initSip getMicGainDb=" + core.getMicGainDb());
        Logger.d(TAG, "initSip PlaybackGainDb=" + core.getPlaybackGainDb());
        //core.enableEchoCancellation(true);
        Logger.d(TAG, "initSip finish Cancellation=" + core.echoCancellationEnabled());
    }
}

SipPhone.java : 登陆

    void login(){
        i("login");
        String username = PreferenceUtils.getStringFromDefault(App.getApp(), App.PREF_VOIP_USER, "");
        String password = PreferenceUtils.getStringFromDefault(App.getApp(), App.PREF_VOIP_PWD, "");
        String domain = PreferenceUtils.getStringFromDefault(App.getApp(), App.PREF_VOIP_IP, "");
        String port = PreferenceUtils.getStringFromDefault(App.getApp(), App.PREF_VOIP_PORT, App.DEF_SIP_PORT);

        if(!StringTools.isNotEmpty(username, password, domain, port)){
            e("login failed: username(" + username + "), password(" + password + "), domain(" + domain + "), port(" + port + ")");
            return;
        }

        //sip:100@192.168.7.119:6060
        if(!domain.contains(":")){
            domain += ":" + port;
        }

        user = factory.createAuthInfo(username, null, password, null, null, domain, null);
        accountParams = core.createAccountParams();
        // A SIP account is identified by an identity address that we can construct from the username and domain
        String sipAddress = "sip:" + username + "@" + domain;
        Address identity = factory.createAddress(sipAddress);
        i("login for address " + sipAddress);

        accountParams.setIdentityAddress(identity);

        // We also need to configure where the proxy server is located
        Address address = factory.createAddress("sip:" + domain);
        // We use the Address object to easily set the transport protocol
        address.setTransport(TransportType.Udp);
        accountParams.setServerAddress(address);
        // And we ensure the account will start the registration process
        accountParams.setRegisterEnabled(true);

        // Asks the CaptureTextureView to resize to match the captured video's size ratio
        //core.getConfig().setBool("video", "auto_resize_preview_to_keep_ratio", true);

        // Now that our AccountParams is configured, we can create the Account object
        Account account = core.createAccount(accountParams);
        //account.setCustomHeader("Header1", "Header2");

        // Now let's add our objects to the Core
        core.addAuthInfo(user);
        core.addAccount(account);

        // Also set the newly added account as default
        core.setDefaultAccount(account);
        core.setUserAgent("User", "Agent");

        // Finally we need the Core to be started for the registration to happen (it could have been started before)
        core.start();
    }
    
    void logout(){
        i("logout");
        Account account = core.getDefaultAccount();
        if(account != null) {
            accountParams = account.getParams().clone();
            accountParams.setRegisterEnabled(false);
            account.setParams(accountParams);
        }
    }

SipPhone.java : 通话部分

//拨打电话.
    @Override
    public void call(String number, boolean video) {
        i("call " + number + " video(" + video + ")");
        String domain = PreferenceUtils.getStringFromDefault(App.getApp(), App.PREF_VOIP_IP, "");
        String port = PreferenceUtils.getStringFromDefault(App.getApp(), App.PREF_VOIP_PORT, App.DEF_SIP_PORT);
        // As for everything we need to get the SIP URI of the remote and convert it to an Address
        String remoteSipUri = "sip:" + toNumber + "@" + domain + ":" + port;
        Address remoteAddress = factory.createAddress(remoteSipUri);
        if(remoteAddress == null)return;
        // If address parsing fails, we can't continue with outgoing call process

        // We also need a CallParams object
        // Create call params expects a Call object for incoming calls, but for outgoing we must use null safely
        CallParams params = core.createCallParams(null);

        // We can now configure it
        // Here we ask for no encryption but we could ask for ZRTP/SRTP/DTLS
        params.setMediaEncryption(MediaEncryption.None);
        params.enableVideo(video);

        //show preview before caling.
        //core.enableVideoPreview(video);

        // Finally we start the call
        core.inviteAddressWithParams(remoteAddress, params);
        //回声消除
        // Call process can be followed in onCallStateChanged callback from core listener
    }

//挂断
    @Override
    public void hangup() {
        i("hangup");
        if (core.getCallsNb() == 0) return;
        // If the call state isn't paused, we can get it using core.currentCall
        Call call = core.getCurrentCall() != null ? core.getCurrentCall() : core.getCalls()[0];
        if(call != null) {
            // Terminating a call is quite simple
            call.terminate();
        }
    }

//接听/应答
    @Override
    public void answer() {
        i("answer");
        if(currentCall != null){
            if(remoteHasVideo()) {
                enableCamera();
                currentCall.getParams().enableVideo(true);
            }
            currentCall.accept();
        }
    }


SipPhone.java : 监听和回调

//在initSip中使用.
    CoreListener coreListener = new CoreListenerStub(){
        @Override
        public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
            d("onCallStateChanged " + state);
            currentCall = call;
            if(state == Call.State.OutgoingProgress){
                //呼出
            }else if(state == Call.State.IncomingReceived){
                //来电
            }else if(state == Call.State.StreamsRunning){
                //通话中, 有音视频流.
            }else if(state == Call.State.UpdatedByRemote){
                //通话变化, 有可能变成语音, 也有可能是带视频...
            }else if(state == Call.State.Released){
                //挂电或结束通话
            }else if(state == Call.State.Error){
                //出错.
            }
        }

        @Override
        public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState state, String message) {
            //message:
            // case "io error": server offline.
            //
            i("onRegistrationStateChanged " + state + " with msg:" + message);
            //((Button)findViewById(R.id.btLogin)).setText(state == RegistrationState.Ok ? "Logout":"Login");
            if(state == RegistrationState.Ok) {
                //登陆成功
            }else{
                //登出
            }
        }
    };

关于视频部分:
如何设置视频显示的控件, 在通话呼起后可以调用这个函数.

    public void setVideoView(View v1, View v2){
        core.setNativePreviewWindowId(v1);
        core.setNativeVideoWindowId(v2);
    }
1
2
3
4
所有的功能接口, 请以参考源码及官方为主
强烈建议下载linphone-android客户端源码并编译运行, 学习如何更好地使用SDK开发自己需要的功能

配置文件
      在优化视频通话的过程中, 接触到关于初始化配置的问题. 很多资料显示, 可能通过配置方件的方式, 配置优化音频参数来优化通话效果:
Echo suppression does not work
Android音视频通话——Linphone开发笔记总结
2022-09-24-voice_communication_audio_codec.md

大致的方法是:

增加配置文件
assets/linphone_factory或 assets/linphonerc_factory
res/raw/linphone_factory 或 res/raw/linphonerc_factory

编写对应配置

[sip]
guess_hostname=1
register_only_when_network_is_up=1
auto_net_state_mon=1
auto_answer_replacing_calls=1
ping_with_options=0
use_cpim=1
zrtp_key_agreements_suites=MS_ZRTP_KEY_AGREEMENT_K255_KYB512
chat_messages_aggregation_delay=1000
chat_messages_aggregation=1

[sound]
#remove this property for any application that is not Linphone public version itself
ec_calibrator_cool_tones=1
# 打开回声消除
echocancellation=1
# MIC 增益
mic_gain_db=0.0
# 回放增益
playback_gain_db=0.0

[video]
displaytype=MSAndroidTextureDisplay
auto_resize_preview_to_keep_ratio=1
max_mosaic_size=vga
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
打包到程序中运行.

以上这些方法, 仅适用于linphone-android客户端源码, 针对基于SDK开发的话, 则需要在对应的地方加入载入配置文件的代码:
//参考
//-linphone-android/app/src/main/java/org/linphone/core/CorePreferences.kt
//-linphone-android/app/src/main/java/org/linphone/LinphoneApplication.kt
//在创建Core之前载入配置文件.
Config config = factory.createConfigWithFactory(App.LINPHONE_CONFIG_DEF, App.LINPHONE_CONFIG_FAC);
core = factory.createCoreWithConfig(config, App.getApp().getActivity());
