package test.action;

import org.junit.Before;
import org.junit.Test;
import org.malai.action.Action;

public abstract class TestAbstractAction<T extends Action> {
	protected T action;

	@Before
	public void setUp() {
		action = createAction();
	}

	protected abstract T createAction();

	@Test
	public abstract void testConstructor() throws Exception;

	@Test
	public abstract void testFlush() throws Exception;

	@Test
	public abstract void testDo() throws Exception;

	@Test
	public abstract void testCanDo() throws Exception;

	@Test
	public abstract void testIsRegisterable() throws Exception;

	@Test
	public abstract void testHadEffect() throws Exception;
}
