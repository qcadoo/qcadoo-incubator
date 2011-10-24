

public class Plugin
{
	String id, group;
	String[] dependencies;

	public Plugin(String id, String[] dependencies, String group) {
		this.id = id;
		this.dependencies = dependencies;
		this.group = group;
	}
	
}
