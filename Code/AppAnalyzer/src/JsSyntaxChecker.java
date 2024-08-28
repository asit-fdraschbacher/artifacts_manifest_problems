import javax.script.*;
import java.io.IOException;
import java.io.InputStream;

import static javax.script.ScriptContext.ENGINE_SCOPE;

public class JsSyntaxChecker {
    private static JsSyntaxChecker INSTANCE = null;

    public static JsSyntaxChecker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JsSyntaxChecker();
        }
        return INSTANCE;
    }

    private ScriptEngine engine;

    private JsSyntaxChecker() {
        try (InputStream jsStream = JsSyntaxChecker.class.getResourceAsStream("esprima.js")){
            String esprima = Utils.loadString(jsStream);
            this.engine = new ScriptEngineManager().getEngineByName("JavaScript");
            ScriptContext context = engine.getContext();
            engine.eval(esprima, context);

            context.setAttribute("__dirname", "/resources", ENGINE_SCOPE);
            context.setAttribute("__filename", "JsSyntaxChecker.js", ENGINE_SCOPE);

            engine.eval(
                    "this.checkSyntaxValid = function(code) {" +
                            "var syntax = esprima.parse(code, { tolerant: true });" +
                            "return syntax.errors.length > 0 ? false : true;" +
                            "};", context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkSyntaxValid(String code) {
        try {
            Invocable inv = (Invocable) engine;
            return (Boolean) inv.invokeFunction("checkSyntaxValid", code);
        } catch (ScriptException | NoSuchMethodException e) {
            return false;
        }
    }
}
