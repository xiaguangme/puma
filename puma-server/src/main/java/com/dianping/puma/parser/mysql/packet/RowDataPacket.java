package com.dianping.puma.parser.mysql.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.utils.PacketUtils;

public class RowDataPacket extends AbstractResponsePacket {

	private static final long serialVersionUID = -4557056126970261798L;

	private boolean ok;

	private List<String> columns = new ArrayList<String>();

	@Override
	protected void doReadPacket(ByteBuffer buf, PumaContext context) throws IOException {
		if (buf.get() == -2) {
			ok = false;
			return;
		}
		buf.position(0);
		while (buf.hasRemaining()) {
			columns.add(PacketUtils.readLengthCodedString(buf));
		}
		ok = true;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "RowDataPacket [columns=" + columns + "]";
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

}