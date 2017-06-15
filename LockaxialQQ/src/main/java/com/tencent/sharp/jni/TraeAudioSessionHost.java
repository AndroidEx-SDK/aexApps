package com.tencent.sharp.jni;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;

public class TraeAudioSessionHost {

	public class SessionInfo {
		public long sessionId;
		// public Context context;
	}

	private ArrayList<SessionInfo> _sessionInfoList = new ArrayList<SessionInfo>();
	private ReentrantLock mLock = new ReentrantLock();

	public SessionInfo find(long nSessionId) {

		SessionInfo si = null;

		mLock.lock();

		for (int i = 0; i < _sessionInfoList.size(); i++) {
			SessionInfo _si = _sessionInfoList.get(i);
			if (_si.sessionId == nSessionId) {
				si = _si;
				break;
			}
		}

		mLock.unlock();

		return si;
	}

	public void add(long nSessionId, Context ctx) {

		if (null != find(nSessionId)) {
			return;
		}

		SessionInfo si = new SessionInfo();
		// si.context = ctx;
		si.sessionId = nSessionId;

		mLock.lock();
		_sessionInfoList.add(si);
		mLock.unlock();
	}

	public void remove(long nSessionId) {

		mLock.lock();

		for (int i = 0; i < _sessionInfoList.size(); i++) {
			if (_sessionInfoList.get(i).sessionId == nSessionId) {
				_sessionInfoList.remove(i);
				break;
			}
		}

		mLock.unlock();
	}

}
