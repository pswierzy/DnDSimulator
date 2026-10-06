package engine.actions;

import engine.context.TurnContext;

public interface IAction {
    boolean canExecute(TurnContext turnContext);
    void execute(TurnContext turnContext);
}
