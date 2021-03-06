/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.command.Command;
import frc.robot.Robot;

/**
 * An example command.  You can replace me with your own command.
 */
public class TargetVisionTest extends Command {
  double lastStrafe = 0;
  long endTime = 0;
  boolean sideChanged = true;
  double maxTA = 10;

  double forward = 0;
  double side = 0;
  double turn = 0;

  public TargetVisionTest() {
    // Use requires() here to declare subsystem dependencies
    requires(Robot.driveSubsystem);
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
    Robot.limelightSubsystem.setTargetMode();
    Robot.visionController.setSetpoint(0);

    if (SmartDashboard.getNumber("kP", 0) == 0 &&
    SmartDashboard.getNumber("kI", 0) == 0 &&
    SmartDashboard.getNumber("kD", 0) == 0 &&
    SmartDashboard.getNumber("kF", 0) == 0) {
      SmartDashboard.putNumber("kP", Robot.visionController.getP());
      SmartDashboard.putNumber("kI", Robot.visionController.getI());
      SmartDashboard.putNumber("kD", Robot.visionController.getD());
      SmartDashboard.putNumber("kF", Robot.visionController.getF());
    }

    if (SmartDashboard.getBoolean("Target Vision: Turn Only", true))
      SmartDashboard.putBoolean("Target Vision: Turn Only", true);

    Robot.visionController.enable();
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    
    double kP = SmartDashboard.getNumber("kP",Robot.vKP);
    double kI = SmartDashboard.getNumber("kI",Robot.vKI);
    double kD = SmartDashboard.getNumber("kD",Robot.vKD);
    double kF = SmartDashboard.getNumber("kF",Robot.vKF);
    Robot.visionController.setPID(kP, kI, kD, kF);

    double ta = Robot.limelightSubsystem.getTA();
    double tx = Robot.limelightSubsystem.getTX();

    if (!SmartDashboard.getBoolean("Target Vision: Turn Only", true)) {
      forward = (Math.abs(ta) > maxTA) ? 0 : percentToTarget(ta,maxTA);
      side = StrafeForTime(Robot.lineSubsystem.MoveToCenter(),500);
    } else {
      forward = 0;
      side = 0;
    }

    turn = (Math.abs(tx) <= Robot.kToleranceDegrees) ? 0 : -Robot.visionPIDTurn;

    Robot.driveSubsystem.arcadeDrive(forward, side, turn);

    SmartDashboard.putNumber("Turn speed", turn);
    SmartDashboard.putNumber("Tx", Robot.limelightSubsystem.getTX());
  }

  double percentToTarget(double value, double target) {
    double sign = (value < 0) ? -1 : 1;
    return ((target - Math.abs(value)) / target) * sign;
  }

  // Returns strafe speed until time runs out and strafe hasn't changed
  private double StrafeForTime(double strafe, int millis) {
    if (strafe != lastStrafe) sideChanged = true;
    lastStrafe = strafe;
    if (sideChanged) {
      endTime = System.currentTimeMillis() + millis;
      sideChanged = false;
    }
    if (System.currentTimeMillis() < endTime)
      return strafe;
    else
      return 0;
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    return false;
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
  }
}
