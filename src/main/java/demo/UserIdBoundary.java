package demo;
/*
	{
		"first":"User",
		"last":"Demo"
	}
 
 */

public class UserIdBoundary {
	private String domain;
	private String email;
	
	public UserIdBoundary() {
	}

	public UserIdBoundary(String domain, String email) {
		super();
		this.domain = domain;
		this.email = email;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "UserIdBoundary [domain=" + domain + ", email=" + email + "]";
	}

	
}
