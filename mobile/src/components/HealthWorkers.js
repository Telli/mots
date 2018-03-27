import React from 'react';
import { View, Text } from 'react-native';
import HealthWorkersList from '../container/HealthWorkersList';

import styles from '../styles/listsStyles';
import getContainerStyle from '../utils/styleUtils';
import commonStyles from '../styles/commonStyles';

const { lightThemeText } = commonStyles;

const HealthWorkers = () => (
  <View style={getContainerStyle()}>
    <Text style={[styles.title, lightThemeText]}>
      Selected CHWs
    </Text>
    <HealthWorkersList selected />
  </View>
);

export default HealthWorkers;
