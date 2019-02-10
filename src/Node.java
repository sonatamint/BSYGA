import java.util.ArrayList;

/* 表示一个节点以及和这个节点相连的所有节点 */
public class Node
{
	public String name = null;
	public ArrayList<Node> relationNodes = new ArrayList<Node>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Node> getRelationNodes() {
		return relationNodes;
	}

	public void setRelationNodes(ArrayList<Node> relationNodes) {
		this.relationNodes = relationNodes;
	}
}