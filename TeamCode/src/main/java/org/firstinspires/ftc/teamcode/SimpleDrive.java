package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;

@TeleOp(name = "Simple Drive", group = "TeleOp")
public class SimpleDrive extends OpMode {
    private DcMotor leftDrive;
    private DcMotor rightDrive;
    private DcMotorEx launch;
    private final double launchTpr = 28;
    private DcMotor intake;
    private CRServo feeder;

    double driveSpeedMultiplier = 1;
    ToggleAction launchToggle;

    @Override
    public void init() {
        leftDrive = hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = hardwareMap.get(DcMotor.class, "right_drive");
        launch = hardwareMap.get(DcMotorEx.class, "launch");
        intake = hardwareMap.get(DcMotor.class, "intake");
        feeder = hardwareMap.get(CRServo.class, "feeder");

        // set rotation directions
        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);
        launch.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        feeder.setDirection(CRServo.Direction.REVERSE);

        // set zero power behaviour
        leftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        launch.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        launchToggle = new ToggleAction();

        telemetry.addData("Status", "Initialized. Ready to drive!");
        telemetry.update();
    }

    @Override
    public void loop() {
        launchToggle.update(gamepad1.a);

        double drive = -gamepad1.left_stick_y;
        double turn = gamepad1.right_stick_x;

        // Calculate power for each side (Arcade Drive math)
        double leftPower = drive + turn;
        double rightPower = drive - turn;

        // Normalise powers if they exceed 1.0 to keep steering responsive
        double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
        if (max > 1.0) {
            leftPower /= max;
            rightPower /= max;
        }

        // Apply power to the motors
        leftDrive.setPower(leftPower * driveSpeedMultiplier);
        rightDrive.setPower(rightPower * driveSpeedMultiplier);
        intake.setPower(1.0);

        if (launchToggle.isActive()) {
            launch.setPower(1.0);
            // allow time for launch motor to get up to speed
            if (launchToggle.getStateDuration() >= 2.5) feeder.setPower(1.0);
        } else {
            launch.setPower(0.0);
            feeder.setPower(0.0);
        }

        // Display telemetry data
        double launchRpm = (launch.getVelocity() / launchTpr) * 60.0;
        telemetry.addData("Drive", "Left (%.0f%%), Right (%.0f%%)", leftPower * 100, rightPower * 100);
        telemetry.addData("Intake", "Motor (%.0f%%)", intake.getPower() * 100);
        telemetry.addData("Launch Mechanism", "Active (%s), Launch (%.0f rpm), Feed (%.0f%%)", launchToggle.isActive(), launchRpm, feeder.getPower() * 100);
        telemetry.update();
    }
}
