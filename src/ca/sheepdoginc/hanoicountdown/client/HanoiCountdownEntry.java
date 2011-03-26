package ca.sheepdoginc.hanoicountdown.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.Date;
import java.util.Stack;

public class HanoiCountdownEntry implements EntryPoint {

  int count = 0;
  Stack<FlowPanel>[] pegs;
  int padding = 32;
  int minDiskWidth = 60;
  int diskHeight;

  FlowPanel[] pegsContainers;

  int disks = 25;
  int pegsCount = 3;
  
  Timer t;

  @Override
  public void onModuleLoad() {
    Date end = new Date("Tue, 10 May 2011 09:00:00 MST");
    //Date end = new Date("Fri, 25 Mar 2011 20:05:00 EST");
    Date start = new Date("Tue, 10 May 2010 09:00:00 MST");
    //Date start = new Date("Fri, 25 Mar 2011 17:00:00 EST");
    
    final DateBox startPick = new DateBox();
    startPick.setValue(start);
    
    final DateBox endPick = new DateBox();
    endPick.setValue(end);
    
    startPick.addValueChangeHandler(new ValueChangeHandler<Date>() {
      
      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        if (t != null) t.cancel();
        init(event.getValue(), endPick.getValue());
      }
    });
    
    endPick.addValueChangeHandler(new ValueChangeHandler<Date>() {
      
      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        if (t != null) t.cancel();
        init(startPick.getValue(), event.getValue());
      }
    });
    
    RootPanel.get("controls").add(startPick);
    RootPanel.get("controls").add(endPick);
    
    init(start, end);
  }

  private void init(final Date start, final Date end) {
    Date now = new Date();

    long secondsDiff = (end.getTime() - start.getTime()) / 1000;
    long untilEnd = (end.getTime() - now.getTime()) / 1000;

    pegs = new Stack[pegsCount];
    pegsContainers = new FlowPanel[pegsCount];

    disks = (int) (Math.log(secondsDiff) / Math.log(2) + 1);
    if (disks % 2 == 0)
      disks++;

    final Double totalMoves = Math.pow(2, disks) - 1;

    count = (int) (totalMoves - untilEnd);
    int moveEveryNMilliSeconds = (int) (1000 * secondsDiff / (totalMoves));
    if (moveEveryNMilliSeconds <= 0)
      moveEveryNMilliSeconds = 1000;

    FlowPanel container = new FlowPanel();

    diskHeight = 350 / disks;
    
    RootPanel.get("towers").clear();
    RootPanel.get("towers").add(container);

    for (int i = 0; i < pegsCount; i++) {
      pegs[i] = new Stack<FlowPanel>();
      FlowPanel pegContainer = new FlowPanel();
      pegContainer.setStyleName("pegContainer");
      pegsContainers[i] = pegContainer;
      container.add(pegContainer);

      if (i != (pegsCount - 1)) {
        pegContainer.getElement().getStyle().setMarginRight(2, Unit.PCT);
      }
    }

    String[] colors = new String[] {
        "#1b53f7", "#ed2833", "#feb513", "#205afc", "#00a516", "#ec2632"};

    for (int j = disks; j > 0; j--) {
      FlowPanel disk = new FlowPanel();
      disk.getElement().getStyle().setBackgroundColor(
          colors[(disks - j) % colors.length]);
      disk.setStyleName("disk");

      Double w = (double) ((j) * 100 / disks);

      disk.setHeight(diskHeight + "px");
      disk.setWidth(w + "%");

      int pos = diskPosition(count, j);

      if (now.getTime() >= end.getTime()) {
        pos = 2;
      }

      pegsContainers[pos].add(disk);
      pegs[pos].add(disk);
    }

    t = new Timer() {

      @Override
      public void run() {
        if (count + 1 > totalMoves) {
          Window.alert("Done!");
          cancel();
          return;
        }

        Date now = new Date();
        long secondsDiff = (end.getTime() - now.getTime()) / 1000;
        int nextMove = (int) (1000 * secondsDiff / (totalMoves - count));

        if (nextMove <= 0)
          nextMove = 1000;

        int x = ++count;
        int from = (x & x - 1) % 3;
        int to = ((x | x - 1) + 1) % 3;
        move(from, to);

        this.schedule(nextMove);
      }
    };
    t.schedule(moveEveryNMilliSeconds);
  }

  private int diskPosition(int move, int disk) {
    return ((((disks + disk + 1) % 2) + 1) * ((move + (int) Math.pow(2,
        disk - 1)) / (int) (Math.pow(2, disk)))) % 3;
  }

  private void move(int from, int to) {
    Stack<FlowPanel> fromPeg = pegs[from];
    Stack<FlowPanel> toPeg = pegs[to];

    FlowPanel disk = fromPeg.pop();
    toPeg.push(disk);

    pegsContainers[from].remove(disk);
    pegsContainers[to].add(disk);
  }

}
