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
import java.util.Stack;
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
	protected static final Coordinate GOAL = new Coordinate(DIMENSION - 1, 2);

	/**
	 * The ID of this board in the database.
	 */
	public int id;

	private DriveListener driveListener;
	private SolveListener solveListener;

	protected Stack<Move> moves = new Stack<Move>();
	private int score;

	public int getScore() {
		return score;
	}

	protected static class Move {
		public final Car car;
		public final Direction direction;

		public Move(Car car, Direction direction) {
			this.car = car;
			this.direction = direction;
		}

		public static enum Direction {
			RIGHT(1), UP(-1), LEFT(-1), DOWN(1);

			public int offset;

			private Direction(int offset) {
				this.offset = offset;
			}

			public Direction opposite() {
				switch (this) {
				case RIGHT:
					return LEFT;
				case UP:
					return DOWN;
				case LEFT:
					return RIGHT;
				case DOWN:
					return UP;
				default:
					throw new IllegalStateException();
				}
			}
		}
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
			Move.Direction direction;
			if (car.canMoveHorizontally()) {
				direction = (offset == -1) ? Move.Direction.LEFT : Move.Direction.RIGHT;
			} else {
				direction = (offset == -1) ? Move.Direction.UP : Move.Direction.DOWN;
			}
			moves.push(new Move(car, direction));
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
	 * Undo a move
	 */
	protected void rollBack(Move move) {
		move.car.move(move.direction.opposite().offset);
	}

	/**
	 * Reset the board
	 */
	public void reset() {
		while (!moves.empty()) {
			rollBack(moves.pop());
		}
	}

	/**
	 * True iff the red car can move out without problems
	 * @return
	 */
	public boolean isSolved() {
		for (Car car : cars) {
			if (car.occupies(GOAL) && car.canMoveHorizontally()) {
				return true;
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
