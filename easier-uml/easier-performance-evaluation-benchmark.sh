#!/bin/bash

CONFIG_FILE=easier-config.conf
RESULT_DIR=/mnt/research/easier/add_search_budget/by_time
#MODELS='/home/peo/git/sealab/easier/easier-uml2lqnCaseStudy/train-ticket/train-ticket.uml /home/peo/git/sealab/easier/easier-uml2lqnCaseStudy/cocome/simplified-cocome/cocome.uml'
MODELS='/home/peo/git/sealab/easier/easier-uml2lqnCaseStudy/cocome/simplified-cocome/cocome.uml'
#ALGOS='NSGA-II SPEA2 PESA2'
ALGOS='NSGA-II'
EVALS='72 82 102'
PROB_PAS='0.55 0.80 0.95'
SB_TIME_TH='3600000 1800000 900000'
#MODEL_NAMES=("ttbs" "cocome")
MODEL_NAMES=("cocome")

function count() {
    echo $#
}
run_total=$(($(count $MODELS)*$(count $ALGOS)*$(count $EVALS)*$(count $PROB_PAS)*$(count $SB_TIME_TH)))

run_count=0
model_count=0
for model in $MODELS; do
    for algo in $ALGOS; do
        for eval_ in $EVALS; do
            for sbTimeTh in $SB_TIME_TH; do
                for probPa in $PROB_PAS; do

            cat << EOF > $CONFIG_FILE
--maxEvaluation
$eval_
--populationSize
16
--solver
/usr/local/bin/lqns
--maxCloning
3
--cloningWeight
1.5
--constChangesWeight
1
-outF
#/tmp/easier-output-PAS/
${RESULT_DIR}/${MODEL_NAMES[$model_count]}/${algo}/${eval_}_eval/${sbTimeTh}/${probPa}/
-tmpF
/tmp/easier-tmp/
--cleaningTmp 
true
--independent_runs
3
-mmp
/home/peo/git/sealab/easier/easier-aemilia/src/main/resources/metamodels/mmAEmilia.ecore
-m
#/home/peo/git/sealab/easier/easier-uml/src/main/resources/models/eshopper/model
#/opt/git/easier/easier-uml2lqnCaseStudy/train-ticket/train-ticket.uml
$model
-oclTemplate
/home/peo/git/sealab/easier/easier-core/src/main/resources/ocl/default/detectionSingleValuePA.ocl
-evlTemplate
/home/peo/git/sealab/easier/easier-epsilon/src/main/resources/refactoring-lib/AP-UML-MARTE.evl
-algo
#Supported algorithms: NSGA-II,SPEA2,R-NSGA,PESA2
$algo
-qI
#SPREAD,IGD+,IGD,EPSILON,HYPER_VOLUME,GENERALIZED_SPREAD
IGD+
-l
4
--genRF
true
--maxWorseModels
5
--uml2lqn
/home/peo/git/sealab/uml2lqn/
--refPoints
#Initial RTs: 2.981(rebook ticket),2.884(update user),2.981(login)
#Inital reliability: 
0.8,2.8320,2882.55,2.8320 
--epsilon
0.3
-brf
#Clone = clone; MvOpToNewCompNewNode = moncnn; MvOpToComp = moc; MvCompNewNode = mcnn
#BRF_clone_1.23__moc_1.64__mcnn_1.45__moncnn_1.80__
clone:1.23;moc:1.64;mcnn:1.45;moncnn:1.80
-probPAS
$probPa
--objectives
4
--search-budget
#byTime, byPrematureConvergence, byBoth
byTime
-sbTimeTh
$sbTimeTh

EOF
                ((run_count++))

                # Run Easier
                echo "Running Easier on model:$model with algorithm:$algo, maxeval:$eval_, probPa:$probPa, sbTimeTh:$sbTimeTh. Run $run_count/$run_total."
                /usr/lib/jvm/java-1.11.0-openjdk-amd64/bin/java -Xmx12g -jar target/easier-uml-0.6.3-jar-with-dependencies.jar @${CONFIG_FILE}
                echo "Running Easier on model:$model with algorithm:$algo, maxeval:$eval_, pobPa:$probPa, sbTimeTh:$sbTimeTh. Run $run_count/$run_total." | mail -s "[EASIER] search-budget done." daniele.dipompeo@univaq.it
                
                cd /home/peo/git/sealab/sbse-project-runs
                git pull
                echo "| ${model} | ${algo}  | ${eval_}  | ${probPa} | ${sbTimeTh} | ${RESULT_DIR}/${MODEL_NAMES[$model_count]}/${algo}/${eval_}_eval/${sbTimeTh}/${probPa}  |" >>  search_budget.md
                git add search_budget.md
                git commit -m "SearchBudget ${sbTimeTh} ${algo} ${eval_} ${probPa} ${model}"
                git push
                cd /home/peo/git/sealab/easier/easier-uml   

                done
            done
        done
    done
   ((model_count++))
done
