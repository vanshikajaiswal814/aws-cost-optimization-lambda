package com.example.awscostoptimisation;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DeleteSnapshotRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSnapshotsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSnapshotsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Snapshot;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeAttachment;

public class DeleteStaleEbsSnapshot implements RequestHandler<Object, String> {

    private final Ec2Client ec2 = Ec2Client.create();

    @Override
    public String handleRequest(Object input, Context context) {

        // Fetch all snapshots owned by this AWS account
        DescribeSnapshotsRequest snapshotsRequest = DescribeSnapshotsRequest.builder()
                .ownerIds("self")
                .build();

        DescribeSnapshotsResponse snapshotsResponse =
                ec2.describeSnapshots(snapshotsRequest);

        List<Snapshot> snapshots = snapshotsResponse.snapshots();

        for (Snapshot snapshot : snapshots) {

            String snapshotId = snapshot.snapshotId();
            String volumeId = snapshot.volumeId();

            try {

                // If snapshot has no associated volume
                if (volumeId == null || volumeId.isEmpty()) {

                    deleteSnapshot(snapshotId);

                    System.out.println(
                            "Deleted snapshot: " + snapshotId +
                            " because no volume was associated.");

                    continue;
                }

                // Check whether volume exists
                DescribeVolumesRequest volumesRequest =
                        DescribeVolumesRequest.builder()
                                .volumeIds(volumeId)
                                .build();

                DescribeVolumesResponse volumesResponse =
                        ec2.describeVolumes(volumesRequest);

                List<Volume> volumes = volumesResponse.volumes();

                // If volume not found
                if (volumes.isEmpty()) {

                    deleteSnapshot(snapshotId);

                    System.out.println(
                            "Deleted snapshot: " + snapshotId +
                            " because volume does not exist.");

                    continue;
                }

                Volume volume = volumes.get(0);

                // Check whether volume is attached
                List<VolumeAttachment> attachments = volume.attachments();

                // If no attachments exist
                if (attachments.isEmpty()) {

                    deleteSnapshot(snapshotId);

                    System.out.println(
                            "Deleted snapshot: " + snapshotId +
                            " because volume is not attached to any instance.");

                    continue;
                }

                boolean attachedToRunningInstance = false;

                // Check if attached to running EC2 instance
                for (VolumeAttachment attachment : attachments) {

                    String instanceId = attachment.instanceId();

                    DescribeInstancesRequest instanceRequest =
                            DescribeInstancesRequest.builder()
                                    .instanceIds(instanceId)
                                    .build();

                    DescribeInstancesResponse instancesResponse =
                            ec2.describeInstances(instanceRequest);

                    for (Reservation reservation : instancesResponse.reservations()) {

                        for (Instance instance : reservation.instances()) {

                            String state =
                                    instance.state().nameAsString();

                            if ("running".equalsIgnoreCase(state)) {

                                attachedToRunningInstance = true;
                                break;
                            }
                        }
                    }
                }

                // Delete snapshot if NOT attached to running instance
                if (!attachedToRunningInstance) {

                    deleteSnapshot(snapshotId);

                    System.out.println(
                            "Deleted snapshot: " + snapshotId +
                            " because volume is not attached to a running instance.");
                }

            } catch (Ec2Exception e) {

                String errorCode = e.awsErrorDetails().errorCode();

                // Handle deleted/missing volume
                if ("InvalidVolume.NotFound".equals(errorCode)) {

                    deleteSnapshot(snapshotId);

                    System.out.println(
                            "Deleted snapshot: " + snapshotId +
                            " because associated volume no longer exists.");
                } else {

                    System.err.println(
                            "Error processing snapshot " +
                            snapshotId + ": " +
                            e.awsErrorDetails().errorMessage());
                }
            }
        }

        return "Snapshot cleanup based on the Condition.";
    }

    // Method to delete snapshot
    private void deleteSnapshot(String snapshotId) {

        DeleteSnapshotRequest deleteRequest =
                DeleteSnapshotRequest.builder()
                        .snapshotId(snapshotId)
                        .build();

        ec2.deleteSnapshot(deleteRequest);
    }
}