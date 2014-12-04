package lhoste.testchtimes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class MainActivity extends Activity {

	private static final String TAG = "chtimes";
	
	class ChtimesRunnable implements Runnable {
		Context mContext;
		String filename;

		ChtimesRunnable(Context mContext, String filename) {
			this.mContext = mContext;
			this.filename = filename;
		}

		@Override
		public void run() {
			DataOutputStream dos = null;
			Process process = null;
			try {
				process = Runtime.getRuntime().exec("sh");
				dos = new DataOutputStream(process.getOutputStream());
				Log.w(TAG, "Launching");
				dos.writeBytes(getApplicationInfo().dataDir + "/lib/chtimes.so " + filename + "\n");
				dos.writeBytes("exit\n");
				dos.flush();
				log(process.getInputStream(), Log.INFO);
				log(process.getErrorStream(), Log.WARN);
				process.waitFor();
				Thread.sleep(300);
				Log.w(TAG, "Done");
			} catch (IOException e) {
				Log.e(TAG, "Failed to execute syncthing binary or read output", e);
			} catch (InterruptedException e) {
				Log.e(TAG, "Failed to execute syncthing binary or read output", e);
			} finally {
				try {
					dos.close();
				} catch (IOException e) {
					Log.w(TAG, "Failed to close shell stream", e);
				}
				process.destroy();
			}
		}
	}

	private void log(final InputStream is, final int priority) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				try {
					while ((line = br.readLine()) != null) {
						Log.println(priority, TAG, line);
					}
				} catch (IOException e) {
					Log.w(TAG, "Failed to read syncthing command line output", e);
				}
			}
		}).start();
	}
	
	private void createFile(String filename) {
		try {
            FileWriter out = new FileWriter(new File(filename));
            out.write("HELLO WORLD!");
            out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String filename = this.getFilesDir() + "/" + "test.txt";
		createFile(filename);
		(new Thread(new ChtimesRunnable(this, filename))).start(); 
		filename = Environment.getExternalStorageDirectory().getParent() + "/0/" + "test.txt";
		createFile(filename);
		(new Thread(new ChtimesRunnable(this, filename))).start();
	}
}
