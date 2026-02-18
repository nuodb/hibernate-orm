// For NUODB

/**
 * Hibernate JARs not yet available, so here is a class similar to
 * DatabaseVersion. Used in hibernate-core.gradle to include, or not,
 * tests based on NuoDB database version.
 */
public class SimpleDatabaseVersion {

	private final int major;
	private final int minor;
	private final int micro;

	public SimpleDatabaseVersion(int major, int minor, int micro) {
		this.major = major;
		this.minor = minor;
		this.micro = micro;
	}

	public SimpleDatabaseVersion(String version) {
		String[] bits = version.split("\\.|-");
		this.major = Integer.parseInt(bits[0]);
		this.minor = Integer.parseInt(bits[1]);
		this.micro = Integer.parseInt(bits[2]);
	}

	public int major() {
		return major;
	}

	public int minor() {
		return minor;
	}

	public int micro() {
		return micro;
	}

	public boolean isBefore(int major, int minor, int micro) {
		return this.major < major || (this.major == major && (this.minor < minor || (this.minor == minor && this.micro < micro)));
	}


	public boolean isAfter(int major, int minor, int micro) {
		return this.major > major || (this.major == major && (this.minor > minor || (this.minor == minor && this.micro > micro)));
	}


	public boolean isEqual(int major, int minor, int micro) {
		return major == this.major && minor == this.minor && micro == this.micro;
	}

	public boolean isSameOrBefore(int major, int minor, int micro) {
		return isEqual(major, minor, micro) || isBefore(major, minor, micro);
	}

	public boolean isSameOrAfter(int major, int minor, int micro) {
		return isEqual(major, minor, micro) || isAfter(major, minor, micro);
	}


	public boolean isBefore(String version) {
		SimpleDatabaseVersion other = new SimpleDatabaseVersion(version);
		return isBefore(other.major(), other.minor(), other.micro());
	}


	public boolean isAfter(String version) {
		SimpleDatabaseVersion other = new SimpleDatabaseVersion(version);
		return isAfter(other.major(), other.minor(), other.micro());
	}

	public boolean isEqual(String version) {
		SimpleDatabaseVersion other = new SimpleDatabaseVersion(version);
		return isEqual(other.major(), other.minor(), other.micro());
	}

	public boolean isSameOrBefore(String version) {
		SimpleDatabaseVersion other = new SimpleDatabaseVersion(version);
		return isSameOrBefore(other.major(), other.minor(), other.micro());
	}

	public boolean isSameOrAfter(String version) {
		SimpleDatabaseVersion other = new SimpleDatabaseVersion(version);
		return isSameOrAfter(other.major(), other.minor(), other.micro());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleDatabaseVersion) {
			SimpleDatabaseVersion sdv = (SimpleDatabaseVersion) other;
			return major == sdv.major && minor == sdv.minor && micro == sdv.micro;
		}

		return false;
	}

	@Override
	public String toString() {
		return "" + major + '.' + minor + '.' + micro;
	}
}
