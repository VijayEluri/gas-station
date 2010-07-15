package com.paintail.android.util;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

public class InfoController extends Thread {
    private Handler handler;
    private final Runnable listener;
    private String url;
    
	private GSInfo gsInfo;
	
    //�R���X�g���N�^
	public InfoController(Handler handler, Runnable listener, String url) {
		this.handler   = handler;
		this.listener  = listener;
		this.url       = url;
		
		//�e���擾�N���X���C���X�^���X��
        gsInfo = new GSInfo();
	}
	
    @Override
    public void run() {
    	gsInfo.setGSInfoList(this.url);
        //�I����ʒm
        handler.post(listener);
    }
    
    //�V�C�񋟒n����擾
	public ArrayList<GSInfo> getGSInfoList() {
		return gsInfo.getGSInfoList();
	}
    
}
