package edu.neu.madcourse.qutang.towerairdrop;

import edu.neu.madcourse.qutang.towerairdrop.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class AcknowledgeActivity extends Activity {
	private TextView mTxtAck;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acknowledge);
		mTxtAck = (TextView) findViewById(R.id.txt_acknowledge);
		StringBuilder text = new StringBuilder();
		text.append("Box2D: open source 2D physical engine\n\n")
			.append("Internet open sourced images and sounds\n  E.g. IconFinder\n\n")
			.append("Internet website for tutorials and codes\n")
			.append("  Stack Overflow\n  Iforce2D\n  Android Official\n  Vogella, et. al.");

		mTxtAck.setText(text);
		Linkify.addLinks(mTxtAck, Linkify.ALL);
	}
}
