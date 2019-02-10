

public class Vertex {//堆位
	
  final private String id;
  final private int ID;
  final private String name;
  public Block occupiedby;//被哪个分段占据
  
  
  public Vertex(String id, String name) {
    this.id = id;
    this.ID = Integer.parseInt(id);
    this.name = name;
  }
  
  public Vertex(Vertex vx) {
	    this.id = vx.id;
	    this.ID = vx.ID;
	    this.name = vx.name;
	    this.occupiedby = vx.occupiedby;
  }
  
  public int getId() {
	  return ID;
  }

  public String getName() {
	  return name;
  }
  
  public int getX() {
	  return (ID-1)/YardState.YLen + 1;
  }

  public int getY() {
	  return (ID%YardState.YLen==0)? YardState.YLen : ID%YardState.YLen;
  }
  
  public int getLeft() {
	  return (ID>YardState.YLen)? ID-YardState.YLen : -1;
  }
  
  public int getRight() {
	  return (ID<YardState.XLen*YardState.YLen-YardState.YLen+1)? ID+YardState.YLen : -1;
  }
  
  public int getUp() {
	  return (ID%YardState.YLen==1)? 0 : ID-1;
  }
  
  public int getDown() {
	  return (ID%YardState.YLen==0)? -1 : ID+1;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }
  

  @Override
  public String toString() {
    return name;
  }
  
  protected Vertex copy() {
	  Vertex newVx = new Vertex(this);
      return newVx;
  }
  
} 