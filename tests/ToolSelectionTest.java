import dev.vortex.client.ToolSelection;

public final class ToolSelectionTest {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) {
        check(ToolSelection.bestSlot(4, new float[]{1,1,1,1,1,1,1,1,1}) == 4);
        check(ToolSelection.bestSlot(0, new float[]{1,2,7,3,1,1,1,1,1}) == 2);
        check(ToolSelection.bestSlot(2, new float[]{7,2,7,3,1,1,1,1,1}) == 2);
        check(ToolSelection.bestSlot(0, new float[]{1,Float.NaN,Float.POSITIVE_INFINITY,2,1,1,1,1,1}) == 3);
        boolean rejected = false;
        try { ToolSelection.bestSlot(9, new float[9]); } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected);
        System.out.println("PASS: faster tool selection, stable ties, invalid speed rejection");
    }
}
