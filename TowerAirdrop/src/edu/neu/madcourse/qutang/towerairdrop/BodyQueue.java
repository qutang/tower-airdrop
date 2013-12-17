package edu.neu.madcourse.qutang.towerairdrop;

import org.jbox2d.dynamics.Body;

public class BodyQueue {

  private int actorID;
  private Body mBody;

  public BodyQueue(int _actorID, Body _body) {
    mBody = _body;
    actorID = _actorID;
  }

  public int getActorID() { return actorID; }
  public Body getBody() { return mBody; }
}