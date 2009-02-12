/*	VIDIS is a simulation and visualisation framework for distributed systems.
	Copyright (C) 2009 Dominik Psenner, Christoph Caks
	This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
	This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
	You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>. */
package vidis.modules.mstAlgorithm;


public class PongPacket extends AMSTPacket {
	private String targetId;
	public PongPacket(int id, String senderId, String targetId) {
		super(Type.PONG, id, senderId);
		this.targetId = targetId;
	}
	public PongPacket(PongPacket p) {
		this(p.getId(), p.getQueryierId(), p.getTargetId());
	}
	public PongPacket(PingPacket p) {
		this(p.getId(), p.getTargetId(), p.getQueryierId());
	}
	public String getTargetId() {
		return targetId;
	}
}