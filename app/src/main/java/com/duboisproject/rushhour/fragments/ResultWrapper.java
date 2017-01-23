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

package com.duboisproject.rushhour.fragments;

/**
 * Wrapper to enable passing of errors from loader.
 */
public final class ResultWrapper<Result> {
	protected Result result;
	protected Exception exception;

	public void setResult(Result result) {
		this.result = result;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Result getResult() throws Exception {
		if (exception != null) {
			throw exception;
		}
		return result;
	}
}
