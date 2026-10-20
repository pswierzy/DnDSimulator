package ai.decider;

import engine.actions.IAction;
import engine.context.TurnContext;
import engine.state.Combatant;

public interface IDecider {
    IAction decideNextAction(Combatant combatant, TurnContext turnContext);
}
