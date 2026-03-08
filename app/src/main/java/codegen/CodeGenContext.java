package codegen;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks JVM local-variable slot assignments for a single method.
 *
 * <p>Encapsulates the two pieces of mutable state that code generation
 * needs — the name-to-slot map and the next available slot index — so
 * they don't need to be threaded through every static method as parameters.
 *
 * <p>Slot 0 is reserved for the {@code String[] args} parameter of the
 * generated {@code main} method; user variables start at slot 1.
 */
public class CodeGenContext {

    private final Map<String, Integer> locals = new HashMap<>();
    private int nextSlot = 1;

    /**
     * Allocates a new local-variable slot for {@code name} and returns it.
     * If the name already occupies a slot (e.g. same name reused across
     * separate if/else branches), the existing slot is reused — this is
     * safe because the semantic analyzer guarantees the two declarations
     * reside in mutually exclusive branches.
     */
    public int allocate(String name) {
        return locals.computeIfAbsent(name, k -> nextSlot++);
    }

    /**
     * Returns the slot previously allocated for {@code name}.
     *
     * @throws IllegalStateException if no slot has been allocated for
     *         {@code name} (indicates a bug upstream — the semantic
     *         analyzer should have caught the undefined variable).
     */
    public int lookup(String name) {
        Integer slot = locals.get(name);
        if (slot == null) {
            throw new IllegalStateException(
                    "Codegen: no slot allocated for variable '" + name + "' — this is a compiler bug");
        }
        return slot;
    }
}
