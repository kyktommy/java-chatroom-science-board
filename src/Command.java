
class Command implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CMDList cmd;
	public Object obj;
	// STring,array,hash
	
	
	public Command(CMDList cmd) {
		this.cmd = cmd;
	}
	
	public void setObject(Object obj) {
		this.obj = obj;
	}
	
	public Object getObject() {
		return this.obj;
	}
}
