package com.example.projectkrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests that connect to the configured Firebase backend.
 *
 * <p>These tests run only as instrumentation tests (androidTest) because they rely on
 * android/google-services configuration and real network calls to Firebase Auth + Firestore.</p>
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseIntegrationTest {

    private static final long TASK_TIMEOUT_SECONDS = 30;

    @Test
    public void firebaseInstances_areAvailable() {
        assertNotNull(FirebaseAuth.getInstance());
        assertNotNull(FirebaseFirestore.getInstance());
    }

    @Test
    public void auth_canSignInAnonymouslyAndSignOut() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser originalUser = auth.getCurrentUser();

        if (originalUser == null) {
            Tasks.await(auth.signInAnonymously(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        FirebaseUser activeUser = auth.getCurrentUser();
        assertNotNull("Expected Firebase user after anonymous sign-in", activeUser);
        assertTrue("Expected anonymous Firebase user", activeUser.isAnonymous());

        if (originalUser == null) {
            Tasks.await(activeUser.delete(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            auth.signOut();
            assertTrue("Expected no Firebase user after cleanup", auth.getCurrentUser() == null);
        }
    }

    @Test
    public void firestore_canWriteReadUpdateAndDeleteDocument_usingAuthenticatedUser() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean signedInByTest = ensureAuthenticatedUser(auth);

        FirebaseUser activeUser = auth.getCurrentUser();
        assertNotNull("FirebaseAuth user should be available for Firestore integration test", activeUser);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String documentId = "androidTest-" + UUID.randomUUID();
        DocumentReference reference = firestore.collection("integration_test").document(documentId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("source", "android-instrumentation");
        payload.put("uid", activeUser.getUid());
        payload.put("status", "created");

        Tasks.await(reference.set(payload), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        DocumentSnapshot snapshot = Tasks.await(reference.get(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue("Expected created document to exist in Firestore", snapshot.exists());
        assertEquals("android-instrumentation", snapshot.getString("source"));
        assertEquals(activeUser.getUid(), snapshot.getString("uid"));
        assertEquals("created", snapshot.getString("status"));

        Tasks.await(reference.update("status", "updated"), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        DocumentSnapshot updatedSnapshot = Tasks.await(reference.get(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertEquals("updated", updatedSnapshot.getString("status"));

        Tasks.await(reference.delete(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        cleanupTestUser(auth, signedInByTest);
    }

    @Test
    public void firestore_canQueryDocumentsCreatedByCurrentUser() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean signedInByTest = ensureAuthenticatedUser(auth);
        FirebaseUser activeUser = auth.getCurrentUser();
        assertNotNull("FirebaseAuth user should be available for Firestore query test", activeUser);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String marker = "androidTest-query-" + UUID.randomUUID();

        DocumentReference docA = firestore.collection("integration_test").document("a-" + UUID.randomUUID());
        DocumentReference docB = firestore.collection("integration_test").document("b-" + UUID.randomUUID());

        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", activeUser.getUid());
        payload.put("marker", marker);

        Tasks.await(docA.set(payload), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Tasks.await(docB.set(payload), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        QuerySnapshot querySnapshot = Tasks.await(
                firestore.collection("integration_test")
                        .whereEqualTo("uid", activeUser.getUid())
                        .whereEqualTo("marker", marker)
                        .get(),
                TASK_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );

        assertTrue("Expected at least two query results for inserted test documents",
                querySnapshot.getDocuments().size() >= 2);

        Tasks.await(docA.delete(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        Tasks.await(docB.delete(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        cleanupTestUser(auth, signedInByTest);
    }

    private boolean ensureAuthenticatedUser(FirebaseAuth auth) throws Exception {
        if (auth.getCurrentUser() == null) {
            Tasks.await(auth.signInAnonymously(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    private void cleanupTestUser(FirebaseAuth auth, boolean signedInByTest) throws Exception {
        if (signedInByTest && auth.getCurrentUser() != null) {
            Tasks.await(auth.getCurrentUser().delete(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            auth.signOut();
        }
    }
}
