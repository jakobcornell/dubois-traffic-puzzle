package com.duboisproject.rushhour.id;

public class DuboisIdentity {
	public static enum Role {
		MATHLETE, COACH;
	}

	public final Role role;
	public final String name;
	public final String id;

	public DuboisIdentity(Role role, String name, String id) {
		this.role = role;
		this.name = name;
		this.id = id;
	}
}
