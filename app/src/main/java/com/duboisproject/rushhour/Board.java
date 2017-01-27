/*
 *     Rush Hour Android app
 * Copyright (C) 2015 Randy Wanga, Jos Craaijo, Camil Staps
 *
 * Modified by Jakob Cornell, 2017-01-25 to -01-26
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

import java.util.HashSet;
import java.util.Set;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.duboisproject.rushhour.GameStatistics;
import com.duboisproject.rushhour.id.Mathlete;
import com.duboisproject.rushhour.database.SdbInterface;

/**
 * Created by camilstaps on 16-4-15.
 */
public class Board {

	Set<Car> cars = new HashSet<>();

	public static final int DIMENSION = 6;

	/**
	 * The ID of this board in the database.
	 */
	public int id;

	private DriveListener driveListener;
	private SolveListener solveListener;

	private int score;

	public int getScore() {
		return score;
	}

	/**
	 * Move a car if possible, and call the appropriate listeners
	 */
	private MoveListener moveListener = new MoveListener() {
		@Override
		public void onMove(Car car, int offset) {
			Coordinate newC = car.wouldMoveTo(offset);
			if (newC.getY() > DIMENSION - 1 || newC.getX() > DIMENSION - 1 || newC.getX() < 0 || newC.getY() < 0) {
				driveListener.onBlocked();
				return;
			}
			for (Car iter : cars) {
				if (iter.occupies(newC)) {
					driveListener.onBlocked();
					return;
				}
			}
			car.move(offset);
			score++;
			if (isSolved() && solveListener != null) {
				solveListener.onSolve(score);
			}
			driveListener.onDrive();
		}
	};

	public Board(Set<Car> cars) {
		for (Car car : cars) {
			add(car);
		}
	}

	public Board() {
		this(new HashSet<Car>());
	}

	public void add(Car car) {
		car.setMoveListener(moveListener);
		cars.add(car);
	}

	/**
	 * Add all cars to an existing layout
	 * RelativeLayout is assumed, although this may work with other Layouts
	 * @param context
	 * @param layout
	 */
	public void addToLayout(Context context, ViewGroup layout) {
		for (Car car : cars) {
			layout.addView(car.getImageView(
				context,
				(layout.getWidth() - layout.getPaddingLeft() - layout.getPaddingRight()) / DIMENSION
			));
		}
	}

	/**
	 * True iff the red car can move out without problems
	 * @return
	 */
	public boolean isSolved() {
		for (int x = DIMENSION - 1; x >= 0; x--) {
			for (Car car : cars) {
				if (car.occupies(new Coordinate(x, 2))) {
					if (car.canMoveHorizontally()) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	public void setDriveListener(DriveListener dl) {
		driveListener = dl;
	}

	/**
	 * Represents a way to describe a board
	 */
	public interface Descriptor {
		/**
		 * Key for use in Intent extra
		 */
		public static final String NAME = "BOARD_DESCRIPTOR";

		/**
		 * Get the associated board. Expect this to use the network.
		 */
		public Board resolve() throws SdbInterface.RequestException;
	}

	/**
	 * Describes a board by its ID (i.e. SDB item name).
	 */
	public static class IdDescriptor implements Descriptor {
		protected int id;
		protected SdbInterface sdbInterface;

		public IdDescriptor(int id, SdbInterface sdbInterface) {
			this.id = id;
			this.sdbInterface = sdbInterface;
		}

		@Override
		public Board resolve() throws SdbInterface.RequestException {
			return sdbInterface.fetchBoard(id);
		}
	}

	/**
	 * Describes a board through a mathlete.
	 * The resolved board is the next one the mathlete should play.
	 */
	public static class ProgressDescriptor implements Descriptor {
		protected Mathlete player;
		protected SdbInterface sdbInterface;

		public ProgressDescriptor(Mathlete player, SdbInterface sdbInterface) {
			this.player = player;
			this.sdbInterface = sdbInterface;
		}

		@Override
		public Board resolve() throws SdbInterface.RequestException {
			GameStatistics stats = sdbInterface.fetchLastPlay(player);

			int targetId;
			if (stats == null) {
				targetId = 0;
			} else {
				targetId = stats.levelId + 1;
			}

			int cappedId = Math.min(targetId, sdbInterface.fetchLevelCount() - 1);
			return sdbInterface.fetchBoard(cappedId);
		}
	}

	public interface SolveListener {
		public void onSolve(int score);
	}

	public void setSolveListener(SolveListener sl) {
		solveListener = sl;
	}
}
