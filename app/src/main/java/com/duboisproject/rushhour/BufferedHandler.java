/*
 * Dubois Traffic Puzzle
 * Jakob Cornell, 2017
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.duboisproject.rushhour;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import android.os.Message;

/*
 * Buffers messages when paused, replays on resume.
 * See <http://stackoverflow.com/a/8122789/2729736>.
 */
public abstract class BufferedHandler extends android.os.Handler {
	protected final Queue<Message> messages = new ConcurrentLinkedQueue<Message>();
	protected boolean paused;

	public final void resume() {
		paused = false;
		while (!messages.isEmpty()) {
			sendMessage(messages.poll());
		}
	}

	public final void pause() {
		paused = true;
	}

	protected abstract void processMessage(Message message);

	@Override
	final public void handleMessage(Message message) {
		if (paused) {
			// not sure why a copy is necessary
			Message copy = new Message();
			copy.copyFrom(message);
			messages.offer(copy);
		} else {
			processMessage(message);
		}
	}
}
