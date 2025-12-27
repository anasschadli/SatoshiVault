package com.wallet.app.service;

import com.wallet.app.model.TransactionModel;
import com.wallet.app.model.TransactionModel.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockTransactionService implements TransactionService {

    @Override
    public List<TransactionModel> getTransactions() {
        List<TransactionModel> transactions = new ArrayList<>();
        
        // Mock transaction data with addresses
        transactions.add(new TransactionModel(
            "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6",
            LocalDateTime.now().minusHours(2),
            0.0025,
            Status.RECEIVED,
            "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"
        ));
        
        transactions.add(new TransactionModel(
            "b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a1",
            LocalDateTime.now().minusHours(5),
            -0.0015,
            Status.SENT,
            "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq"
        ));
        
        transactions.add(new TransactionModel(
            "c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a1b2",
            LocalDateTime.now().minusDays(1),
            0.0500,
            Status.RECEIVED,
            "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4"
        ));
        
        transactions.add(new TransactionModel(
            "d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a1b2c3",
            LocalDateTime.now().minusDays(2),
            -0.0100,
            Status.SENT,
            "bc1q5shngj93fzv7577flue9r8u3haq6lxkws8me3j"
        ));
        
        transactions.add(new TransactionModel(
            "e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a1b2c3d4",
            LocalDateTime.now().minusDays(3),
            0.1000,
            Status.RECEIVED,
            "bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3"
        ));
        
        transactions.add(new TransactionModel(
            "f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a1b2c3d4e5",
            LocalDateTime.now().minusDays(5),
            -0.0200,
            Status.SENT,
            "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c"
        ));
        
        transactions.add(new TransactionModel(
            "g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a1b2c3d4e5f6",
            LocalDateTime.now().minusDays(7),
            0.0750,
            Status.RECEIVED,
            "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak"
        ));
        
        transactions.add(new TransactionModel(
            "h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a1b2c3d4e5f6g7",
            LocalDateTime.now().minusDays(10),
            -0.0050,
            Status.SENT,
            "bc1q2xtn5zfj8y6j4xkq63eukzjr3hw8sjt3pykg3u"
        ));
        
        transactions.add(new TransactionModel(
            "i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a1b2c3d4e5f6g7h8",
            LocalDateTime.now().minusDays(15),
            0.0300,
            Status.RECEIVED,
            "bc1qc7slrfxkknqcq2jevvvkdgvrt8080852dfjewde450xdlk4ugp7szw5tk9"
        ));
        
        transactions.add(new TransactionModel(
            "j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a1b2c3d4e5f6g7h8i9",
            LocalDateTime.now().minusDays(20),
            -0.0075,
            Status.PENDING,
            "bc1q9g0q7qyvjk9xc9vtxy8zr9w7nza4a5y9c3ckl3"
        ));
        
        return transactions;
    }

    @Override
    public boolean sendTransaction(String address, double amount) {
        // Mock implementation - always returns true
        return true;
    }
}
