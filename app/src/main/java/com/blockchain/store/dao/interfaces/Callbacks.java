package com.blockchain.store.dao.interfaces;

import android.util.Pair;

import com.blockchain.store.dao.data.TokenBalance;
import com.blockchain.store.dao.database.model.Proposal;

public abstract class Callbacks {

    public interface ProposalCallback {

        void onItemClicked(Proposal proposal);

    }

    public interface PasswordCallback {

        void onAccountUnlock(Boolean isUnlock);

    }

    public interface VotesCallback {

        void OnRecyclerViewScrolled(int y);

    }

    public interface DaoTokenCallback {

        void onBalanceReady(TokenBalance tokenBalance);

        void onBalanceFailed(Throwable throwable);

    }

}
